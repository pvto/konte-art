
package org.konte.generate;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.konte.lang.Language;
import org.konte.misc.Matrix4;
import org.konte.model.Model;
import org.konte.parse.ParseException;
import org.konte.model.RepeatStructure;
import org.konte.model.Rule;
import org.konte.model.Transform;
import org.konte.model.DrawingContext;
import org.konte.model.NonDeterministicRule;
import org.konte.expression.BooleanExpression;
import org.konte.expression.Name;
import org.konte.image.OutputShape;
import org.konte.lang.Tokens.Constant;
import org.konte.model.ConditionalStructure;
import org.konte.model.Untransformable;
import org.konte.model.MeshIndex;
import org.konte.model.PathRule;
import org.konte.plugin.KontePluginScript;

/**
 *
 * @author pto
 */
public class RuleWriter {

    public Model model;
    private RandomFeed rndFeed;
    private ShapeReader sr;
    public List<OutputShape> shapes;
    private LinkedList<Expansion> expansions;
    private OutQueueList outoftheway;
    private List<File> expansionTmpFiles;
    private List<File> handledExpansionTmpFiles = new ArrayList<File>();
//    public DrawingContext model.context;
    private Transform zeroTransform = new Transform();
    private MeshIndex meshes;

    public final Object lock1 = new Object();
    public final Object shapenfy = new Object();

    private int forwardedShapes;
    private int generatedExpansions;
    boolean drained = false;

    public RuleWriter(Model model) throws ParseException, IOException {
        this.model = model;
        resetShapes();
    }

    public void finish() {
        drained = true;
    }

    private static class OutQueueList extends ArrayList<LinkedList<Expansion>> {
        private int count = 0;
        @Override
        public boolean add(LinkedList<Expansion> e) {
            count += e.size();
            return super.add(e);
        }
        
        public int totalSize() {
            return count;
        }
        @Override
        public void clear() {
            super.clear();
            count = 0;
        }
        public Expansion remove() {
            LinkedList<Expansion> l = get(0);
            Expansion e = l.removeFirst();
            if (l.size() == 0)
                remove(0);
            count--;
            return e;
        }

    }

    public void init(ShapeReader sr) throws ParseException {
        expansions = new LinkedList<Expansion>();
        outoftheway = new OutQueueList();
        expansionTmpFiles = new ArrayList<File>();
        zeroTransform.initialize(model);
        meshes = new MeshIndex(model);
        if (rndFeed == null) {
            setRandomFeed(new RandomFeed());
        }
        this.setShapeReader(sr);
        sr.setRuleWriter(this);
        new File("tmp").mkdir();
    }

    CountDownLatch countdown = new CountDownLatch(1);
    public void generate() throws ParseException {
        try {
            countdown.await(100, TimeUnit.MILLISECONDS);
        } catch(InterruptedException ie) {
            throw new ParseException("countdown.await");
        }
        
        model.context = new DrawingContext();
        model.context.matrix = Matrix4.IDENTITY;
        forwardedShapes = 0;
        generatedExpansions = 0;

        drained = false;
        ObjectInputStream ois = null;
        int nToOut = MemoryWatcher.getMoveCountEstimate() / 250;
        int tmpFileSz = MemoryWatcher.getMoveCountEstimate() / 3;

        NonDeterministicRule ndr = model.rules.get(model.startshape);
        processRule(ndr.randomRule(this.rndFeed));
        
        while (!drained && forwardedShapes < model.maxShapes) {

            if (expansions.size() > 0) {
                Expansion e = expansions.removeFirst();    //expansions.size()-1);
                model.context = e.point;
                Rule r = model.indexedNd[e.ndruleIndex].randomRule(rndFeed);
                processRule(r);
                if (expansions.size() > nToOut) {
                    if (outoftheway.totalSize() > tmpFileSz) {
                        try {
                            Runtime.sysoutln(getStats());
                            ObjectOutputStream oos = nextOos(null);
                            int count = 0;
                            int fileShapes = tmpFileSz/2;
                            for(List<Expansion> l : outoftheway)
                                for (Expansion exp : l) {
                                    oos.writeObject(exp);
                                    count++;
                                    if (count >= fileShapes) {
                                        oos = nextOos(oos);
                                        count = 0;
                                    }
                                }
                            outoftheway = new OutQueueList();
                            oos.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        outoftheway.add(expansions);
                        expansions = new LinkedList<Expansion>();
                    }
                }
            } else {
                if (outoftheway.size() > 0) {
                    Expansion e = outoftheway.remove(); 
                    model.context = e.point;
                    Rule r = model.indexedNd[e.ndruleIndex].randomRule(rndFeed);
                    processRule(r);
                } else if (this.expansionTmpFiles.size() > 0 || ois != null) {
                    boolean closeOis = false;
                    try {
                        if (ois == null) {
//                            Runtime.sysoutln(getStats() + ", loading " + expansionTmpFiles.get(0).getName(), 0);
                            File file = expansionTmpFiles.remove(0);
                            handledExpansionTmpFiles.add(file);
                            ois = new ObjectInputStream(
                                    new BufferedInputStream(
                                    new FileInputStream(file)));
                        }
                        int count = 0;
                        for (;;) {
                            if (count++ > 1000) {
                                break;
                            }
                            Expansion f = (Expansion) ois.readObject();
                            if (f == null) {
                                closeOis = true;
                                break;
                            }
                            expansions.add(f);
                        }
                    } catch(Exception ex) {
                        closeOis = true;
                    } finally {
                        if (closeOis)
                        {
                            if (ois != null)
                                try {
                                    ois.close();
                                } catch(Exception ex) {}
                            ois = null;
                        }
                    }                
                } else { 
                    drained = true;
                }
            } 
        }
        this.meshes = null;
        expansions = null;
        outoftheway = null;
        deleteTmpFiles();
        printStats();
//        Runtime.sysoutln("Rulewriter finished", 1);
        while(sr.state()==1 || sr.state()==2) {
            if (sr.state() < 2) sr.finish(2);
            synchronized(lock1) {
                lock1.notifyAll();
            }
        }
        resetShapes();
        expansionTmpFiles = null;
    }
    private int tmpfc = 0;

    private void deleteTmpFiles() {
        List<File> list = handledExpansionTmpFiles;
        list.addAll(expansionTmpFiles);
        if (list.size() > 0) {
            Runtime.sysoutln(String.format("Deleting %d temp files",list.size()), 10);
            for (File f : list) {
                f.delete();
            }
        }
    }

    private ObjectOutputStream nextOos(ObjectOutputStream oos) throws IOException {
        if (oos != null) {
            try {
                oos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Runtime.sysoutln(getStats() + ", writing tempfile " + ++tmpfc, 10);
        File filee = File.createTempFile("c3dg-"+tmpfc, "tmp");
        expansionTmpFiles.add(filee);
        //FileWriter wr = new FileWriter(new File(fname));
        BufferedOutputStream bo = new BufferedOutputStream(new FileOutputStream(filee));
        ObjectOutputStream ret = new ObjectOutputStream(bo);
        return ret;

    }

    private void processRule(Rule r) throws ParseException {
        for (BooleanExpression be : r.pre) {
            if (!be.bevaluate()) {
                return;
            }
        }
        for (Constant c: r.macros) {
            model.setConstant(c.name, c.value);
        }
        if (r.scripts != null) {
            for(KontePluginScript script : r.scripts) {
                script.execute();
            }
        }
        if (r instanceof PathRule) {
            PathRule pr = (PathRule)r;
            if (pr.closed > 0) {
                processClosedPath(pr);
            }
        }
        for (Transform st : r.transforms) {
            processShapeTransform(st, r.post);
        }
    }
    
    private void addShape(OutputShape s) {
        if (shapes.size() < 1000 || !(sr instanceof StreamingShapeReader)) {
            shapes.add(s);
        } else {
            addShapeAndSubmit(s);
        }
    }
    private void addShapeAndSubmit(OutputShape s) {
        synchronized(lock1) {
            shapes.add(s);
            lock1.notifyAll();
        }
    }
    public List<OutputShape> exchangeShapes() throws InterruptedException {
        List<OutputShape> ret = null;
        synchronized(lock1)
        {
            lock1.wait();
            ret = shapes;
            resetShapes();
            lock1.notifyAll();
        }
        return ret;
    }
    public void resetShapes() {
        synchronized(lock1) {
            shapes = new LinkedList<OutputShape>();
        }
    }
    
    private void processShapeTransform(Transform st, List<BooleanExpression> post)
            throws ParseException {
        if (model.context.d == 0) {
            return;
        }
        if (st.terminatingShape) {
            if (post != null) {
                for (BooleanExpression be : post) {
                    if (!be.bevaluate()) {
                        return;
                    }
                }
            }
            DrawingContext p = st.transform(model.context);
            DrawingContext tmp = model.context;
            model.context = p;
            p.applyShading(model);
            model.lighting.lightObject(p);
            model.context = tmp;
            if (p.shape==Language.MESH) {
                meshes.add(p);
            }
            p.isDrawPhase = 1;
            addShape(p.toOutputShape());
            forwardedShapes++;
        } else if (st.repeatStructure) {
            RepeatStructure rr = (RepeatStructure) st;
            DrawingContext repPoint = model.context;
            int repeats = (int) Math.floor(rr.repeats.evaluate());
            for (int i = 0; i < repeats; i++) {
                if (post != null) {
                    for (BooleanExpression be : post) {
                        if (!be.bevaluate()) {
                            continue;
                        }
                    }
                }
                processShapeTransform(rr.repeatedTransform, null);
                model.context = rr.repeatTransform.transform(model.context);
                if (model.context.d > -1) {
                    model.context.d ++;
                }
                if (drained) {
                    break;
                }
            }
            model.context = repPoint;
        } else if (st.conditionalStructure) {
            ConditionalStructure c = (ConditionalStructure)st;
            if (!c.conditional.bevaluate())
                return;
            DrawingContext repPoint = model.context;
            for(Transform t : c.onCondition) {
                processShapeTransform(t, post);
                model.context = repPoint;
            }
        } else {
            Expansion e = null;
            if (st.indexedNd < 0) { // PEEK,POP
                DrawingContext p = st.transform(model.context);
                if (st.poppedContinuation < 0) {
                    if (st.poppedContinuation == Integer.MIN_VALUE) {
                        return;
                    } // continuation stack had been empty
                    for (Untransformable u : Language.untransformables()) {
                        if (u.getId() == st.poppedContinuation) {
                            p.shape = u;
                            DrawingContext tmp = model.context;
                            p.applyShading(model);
                            model.lighting.lightObject(p);
                            model.context = tmp;
                            if (u==Language.MESH) {
                                meshes.add(p);
                            }
                            p.isDrawPhase = 1;
                            addShape(p.toOutputShape());
                            forwardedShapes++;
                            return;
                        }
                    }
                    throw new ParseException("Untransformable id not found: " + st.poppedContinuation);
                }
                e = new Expansion(p, st.poppedContinuation);
            } else {
                e = new Expansion(st.transform(model.context), st.indexedNd);
            }
            if (e.point.getMinWidth() >= model.minfeaturesize) {
                if (post != null) {
                    for (BooleanExpression be : post) {
                        if (!be.bevaluate()) {
                            return;
                        }
                    }
                }
                expansions.add(e);
            }
            generatedExpansions++;
        }
    }

    private void processClosedPath(PathRule pr) throws ParseException {
        DrawingContext cur = model.context;
        Untransformable ut;
        if (pr.closed == 2) {
            ut = Language.getUntransformable(pr.id);
        } else {
            ut = pr.createUntransformable();
        }
        cur.shape = ut;
        cur.applyShading(model);
        model.lighting.lightObject(cur);
        cur.isDrawPhase = 1;
        addShape(cur.toOutputShape());
        forwardedShapes++;
    }
    
    public void printStats() {
        Runtime.sysoutln(getStats(), 0);
    }

    public String getStats() {
        return String.format("%d shapes, %d expansions", forwardedShapes, generatedExpansions);
    }
    
    public boolean drained() { return drained; }

    public void setAsLocalConstantSource() {
        Name.gene = this;
    }

    public void setShapeReader(ShapeReader sr) {
        this.sr = sr;
    }

    public RandomFeed getRandomFeed() {
        return rndFeed;
    }

    public void setRandomFeed(RandomFeed randomFeed) {
        this.rndFeed = randomFeed;
    }



    public static class Tmper
            implements Runnable {

        RuleWriter rw;

        public Tmper(RuleWriter rw) {
            this.rw = rw;
        }

        public void run() {
            try {
                rw.generate();
            } catch (Exception ex) {
                Runtime.sysoutln(ex.getMessage() + "", 10);
                ex.printStackTrace();
            }
        }
    }
}
