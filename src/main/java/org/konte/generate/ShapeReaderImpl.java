
package org.konte.generate;

import org.konte.image.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.konte.image.OutputShape;
import org.konte.model.Model;
import struct.quadtree.Octree;


/** @author pvto */
public abstract class ShapeReaderImpl implements ShapeReader{

    protected HashMap<Float,Layer> layers =
            new HashMap<Float, Layer>();
    protected ArrayList<Layer> layerInd =
            new ArrayList<Layer>();
    protected Canvas canvas;
    protected Model model;
    protected long count = 0;
    private int state = 0;
    private TreeSet<TmpFile> tmpFiles;

    private boolean drawIntermediate = false;
    private boolean tmpFilePhase = false;

    private ExecutorService drawer = Executors.newSingleThreadExecutor();
    private RuleWriter rulew;
    private long shapeCount;
    private boolean enableLaterIteration = false;
    
    
    public ShapeReaderImpl(Model model)
    {
        this.model = model;
        tmpFiles = new TreeSet<ShapeReaderImpl.TmpFile>();
    }

    public void setRuleWriter(RuleWriter rulew)
    {
        this.rulew = rulew;
    }

    @Override
    public RuleWriter getRuleWriter() {
        return this.rulew;
    }

    
    public void setEnableLaterIteration(boolean enable)
    {
        this.enableLaterIteration = enable;
    }
    
    public long getShapeCount()
    {
        return shapeCount;
    }

    public Canvas getCanvas()
    {
        return canvas;
    }

    public void setCanvas(Canvas canvas)
    {
        this.canvas = canvas;
    }

    public int state()
    {
        return state;
    }

    
    protected void drawLastToFirst()
    {
        Runtime.sysoutln(String.format("Drawing  (%s;%s)",
                this.getClass().getSimpleName(),
                canvas.getClass().getSimpleName()),5);
        if (tmpMin.size()>0 && !drawIntermediate)
        {
            processTmpFiles(false);
            drawer.shutdown();
        }
        else
        {
            final ArrayList<Layer> lls = layerInd;
            if (!enableLaterIteration)
            {
                layerInd = new ArrayList<Layer>();
            }
            layers = new HashMap<Float,Layer>();
            Runnable rble =
            new Runnable()
            {
                public void run()
                {
                    long then = shapeCount;
                    for (Layer ll : lls)
                    {
                        canvas.initLayer(model, ll.layerIndex);
                        Entry<Float, ? extends List<OutputShape>> e = ll.points.lastEntry();
                        while (e != null && state == 3)
                        {
                            for(OutputShape p : e.getValue())
                            {
                                p.shape.draw(model.cameras.get(p.fov),canvas,p);
                                shapeCount++;
                            }
                            e = ll.points.lowerEntry(e.getKey());
                        }
                        if (!enableLaterIteration)
                        {
                            ll.points.clear();
                        }
                        canvas.applyEffects(model, ll.layerIndex);
                    }
                    Runtime.sysoutln("drawn: " + (shapeCount-then) + " state=" + state, 0);
                    if (!drawIntermediate && state == 3)
                    {
                        state = 5;  // finish
                    }
                }
            };
            drawer.submit(rble);
        }
        if (!drawIntermediate)
        {
            delTmpFiles();
        }
    }
    




    protected void drawFirstToLast()
    {
        Runtime.sysoutln("Drawing (first-to-last order)",5);
        if (tmpMin.size()>0 && !drawIntermediate)
        {
            processTmpFiles(true);
            drawer.shutdown();
        }
        else
        {
            final ArrayList<Layer> lls = layerInd;
            if (!enableLaterIteration)
            {
                layerInd = new ArrayList<Layer>();
            }
            layers = new HashMap<Float,Layer>();
            Runnable rble = new Runnable()
            {
                public void run()
                {
                    long then = shapeCount;
                    for (Layer ll : lls)
                    {
                        canvas.initLayer(model, ll.layerIndex);
                        Entry<Float, ? extends List<OutputShape>> e = ll.points.firstEntry();
                        while (e != null && state == 3)
                        {
                            for(OutputShape p : e.getValue())
                            {
                                //canvas.drawShape(model.cameras.get(p.fov), p);
                                p.shape.draw(model.cameras.get(p.fov),canvas,p);
                                shapeCount++;
                            }
                            e = ll.points.higherEntry(e.getKey());
                        }
                        if (!enableLaterIteration)
                        {
                            ll.points.clear();
                        }
                        canvas.applyEffects(model, ll.layerIndex);
                    }
                    Runtime.sysoutln("drawn: " + (shapeCount-then) + " state=" + state, 0);
                    if (!drawIntermediate && state == 3)
                    {
                        state = 5;  // finish
                    }
                }
            };
            drawer.submit(rble);
        }
        if (!drawIntermediate)
        {
            delTmpFiles();
        }
    }

       
    private void delTmpFiles()
    {
        
        for(TmpFile file : tmpMin.values())
        {
            file.file.delete();
            Runtime.sysoutln("deleted " + file.file, 0);
        }
    }

    private long processTmpFiles(boolean firstToLast)
    {
        tmpFilePhase = true;
        try
        {
            moveToTmpFiles();
            if (firstToLast)
                processFirstToLast();
            else
                processLastToFirst();
        } 
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return 0;
    }
    
    private long processFirstToLast() throws InterruptedException
    {
        long l = 0;
        Entry<Order,TmpFile> curMin = tmpMin.lastEntry();
        Entry<Order,TmpFile> curMax = tmpMax.lastEntry();
        ArrayList<TmpFile> openFiles = new ArrayList<TmpFile>();
        TmpFile curTmp = null;
        ObjectInputStream ois = null;
        boolean finished = false;
        for(;;)
        {
            for(;;)
            {
//                Runtime.sysoutln(cnt + " processed",0);
                if(curMin!=null && curMin.getKey().compareTo(curMax.getKey()) >= 0) 
                    break;
                if (openFiles.size()==0)
                {
                    try
                    {
                        if ((ois = curMin.getValue().openTmpFile()) == null)
                            break;
                        Runtime.sysoutln(curMin.getValue().file,0);
                    } 
                    catch(NullPointerException npe)
                    {
                        break;
                    }
                    openFiles.add(curTmp = curMin.getValue());  
                    curMin = tmpMin.higherEntry(curMin.getKey());
                } 
                else
                {
                    boolean match = false;
                    Iterator<TmpFile> it = openFiles.iterator();
                    while(it.hasNext())
                    {
                        TmpFile f = it.next();
                        if(f.current.compareToWeak(curMax.getKey()) <= 0)
                        {
                            curTmp = f;
                            ois = f.ois;
                            //System.out.println("Resuming at " + f.file + "  " + f.current);
                            match = true;
                            break;
                        }
                    }
                    if (!match)
                    {
                        TmpFile tmf;
                        if (curMin==null)
                            break;
                        tmf = curMin.getValue();
                        if ((ois = tmf.openTmpFile()) == null)
                            break;
                        Runtime.sysoutln(tmf.file,0);
                        openFiles.add(curTmp = tmf); 
                        curMin = tmpMin.higherEntry(curMin.getKey());
                    }
                }
                OutputShape p;
                Order o;
                boolean toEnd = true;
                try
                {
                    while ((o = (Order) ois.readObject()) != null)
                    {
                        p = (OutputShape) ois.readObject();
                        //canvas.drawShape(model.cameras.get(p.fov), p);
                        //p.shape.draw(model.cameras.get(p.fov),canvas,p);
                        //shapeCount++;
                        addShape2(p);
                        l++;
                        if (o.compareToWeak(curMax.getKey()) > 0)
                        {
                            curTmp.current = o;
                            toEnd = false;
                            break;
                        }
                        if (state != 3)
                            break;
                    }
                }
                catch(EOFException ex)
                {
                    ex.printStackTrace();
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                } 
                finally
                {
                    try
                    {
                        if (toEnd)
                        {
                            ois.close(); 
                            curTmp.file.delete();
                            openFiles.remove(curTmp);
                            curTmp.ois = null;
                            curTmp = null;
                        }
                    } 
                    catch(Exception e)
                    {
                        e.printStackTrace();
                        break;
                    }
                }
                if (curMin == null || 
                        curMin.getKey().compareToWeak(curMax.getKey()) >= 0)
                {
                    drawIntermediate = true;
                    drawFirstToLast();
                    if (openFiles.size() == 0)
                        break;
                }
            }
            if ((curMax == null || (curMax = tmpMax.higherEntry(curMax.getKey())) == null) && openFiles.size() == 0)
                break;
        }
        Runtime.sysoutln(openFiles.size() + " open; " + tmpMax.size() + " in tmpMax; + addedCount2 " + addedCount2, 0);
        return l;
        
    }
    private void processLastToFirst() throws InterruptedException
    {
        Entry<Order,TmpFile> curMin = tmpMin.firstEntry();
        Entry<Order,TmpFile> curMax = tmpMax.firstEntry();
        ArrayList<TmpFile> openFiles = new ArrayList<TmpFile>();
        TmpFile curTmp = null;
        ObjectInputStream ois = null;
        
        for(;;)
        {
            for(;;)
            {
                if (openFiles.size()==0)
                {
                    if (curMax == null)
                        break;
                    TmpFile tmp = curMax.getValue();
                    Runtime.sysoutln(tmp.file + " open - " + ois,0);
                    ois = tmp.openTmpFile();
                    openFiles.add(curTmp = tmp);
                    curMax = tmpMax.higherEntry(curMax.getKey());
                } 
                else
                {
                    boolean match = false;
                    Iterator<TmpFile> it = openFiles.iterator();
                    while(it.hasNext())
                    {
                        TmpFile f = it.next();
                        if(f.current.compareTo(curMin.getKey()) <= 0)
                        {
                            curTmp = f;
                            ois = f.ois;
                            Runtime.sysoutln("Resuming at " + f.file + "  " + f.current,0);
                            match = true;
                            break;
                        }
                    }
                    if (!match)
                    {
                        TmpFile tmf;
                        if (curMax==null)
                            break;
                        tmf = curMax.getValue();
                        ois = tmf.openTmpFile();
                        Runtime.sysoutln(tmf.file + " opened ",0);
                        openFiles.add(curTmp = tmf); 
                        curMax = tmpMax.higherEntry(curMax.getKey());
                        
                    }
                }
                OutputShape p;
                Order o;
                boolean toEnd = true;
                long l = 0;
                try
                {
                    while ((o = (Order) ois.readObject()) != null)
                    {
                        p = (OutputShape) ois.readObject();
                        //canvas.drawShape(model.cameras.get(p.fov), p);
//                        p.shape.draw(model.cameras.get(p.fov),canvas,p);
//                        shapeCount++;
                        addShape2(p);
                        l++;
                        if (o.compareToWeak(curMin.getKey()) > 0)
                        {
                            curTmp.current = o;
                            toEnd = false;
                            break;
                        }                            
                        if (state != 3)
                            break;
                    }
                }
                catch(EOFException ex)
                {
                    Runtime.sysoutln("EOF " + curTmp.file + "=" + l, 0);
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                } finally
                {
                    try
                    {
//                        Runtime.sysoutln(l + " added", 0);
                        if (toEnd)
                        {
                            ois.close();
                            ois = null;
                            curTmp.file.delete();
                            openFiles.remove(curTmp);
                            curTmp.ois = null;
                            curTmp = null;
                        }
                    } 
                    catch(Exception e)
                    {
                        e.printStackTrace();
                        break;
                    }
                }
                if (curMax == null ||
                        curMax.getKey().compareToWeak(curMin.getKey()) >= 0)
                {
                    drawIntermediate = true;
                    drawLastToFirst();
                    if (openFiles.size() == 0)
                        break;
                }
            }
            if ((curMin == null || (curMin = tmpMin.higherEntry(curMin.getKey())) == null) && openFiles.size() == 0)
            {
                break;
            }
                
        }
        drawIntermediate = true;
        drawLastToFirst();
        Runtime.sysoutln(openFiles.size() + " open; " + tmpMax.size() + " in tmpMax; + addedCount2 " + addedCount2, 0);
    }
    
    protected class Layer implements Comparable<Layer>
    {

        NavigableMap<Float, List<OutputShape>> points;
        float layerIndex;

        public Layer(NavigableMap<Float, List<OutputShape>> points, float layerIndex)
        {
            this.points = points;
            this.layerIndex = layerIndex;
        }

        public int compareTo(Layer o)
        {
            boolean b = layerIndex > o.layerIndex;
            if (b) return 1;
            else if (layerIndex < o.layerIndex) return -1;
            return 0;
        }
        
    }
    
    
    @Override
    public void finish(int step)
    {
        Runtime.sysoutln("Stopping generator " + (rulew != null ? ("(" + rulew.getStats() + ")") : ""),5);
        state = step;
    }

    
    public void rewind()
    {
        finish(5);
    }
    /**This template method polls shapes from RuleWriter, and, depending on
     * Brushmode, either draws some immediately on the canvas, or delays
     * all drawing till RuleWriter is exhausted.
     * 
     */
    @Override
    public void run()
    {
        rulew.countdown.countDown();
        state = 1;
        moveCountInt = 500000;
        Throwable t = null;
        Runtime.sysoutln("Shapes to tmp:" + moveCountInt,10);
        for(;;)
        {
            try
            {
                List<OutputShape> shapes = rulew.exchangeShapes();
                int i = 0;
                for(OutputShape p : shapes)
                {
                    if (i++ % 20 == 0)
                        try
                        {
                            p.shape.draw(model.cameras.get(p.fov), canvas, p);
                        } 
                        catch(Exception e)
                        {
                            Runtime.sysoutln("Unable to draw shape " + p.shape.name + " ["+ p.shape.getClass() + "]", 20);
                            throw e;
                        }
                    addShape(p);
                }

            } 
            catch(Exception e)
            {
                e.printStackTrace();
                t = e;
            }

            if (state > 1)
            {
                break;
            }
        }
        if (t != null)
        {
            state = 0;
            return;
        }
        Runtime.sysoutln("sr1 " + state, 0);
        for (OutputShape p : rulew.shapes)
        {
            addShape(p);
            if (state > 2)
            {
                Runtime.sysoutln("sr1.1 " + state, 0);
                state = 0;
                return;
            }
        }
        try {
            rulew.exchangeShapes();
        }
        catch(InterruptedException ie)
        {
            throw new RuntimeException("ShapeReader:run:draw remaining");
        } 
        Runtime.sysoutln("sr2 " + state, 0);
        if (state < 3)
        {
            canvas.init(canvas.getWidth(), canvas.getHeight());
            state = 3;
            drawRemainingShapes();
        }
        Runtime.sysoutln("sr3 " + state, 0);
        while (state == 3 && !drawer.isTerminated())
            try
            {
                drawer.awaitTermination(10L, TimeUnit.MILLISECONDS);
            }
            catch(InterruptedException ex)
            {
                ex.printStackTrace();
            }
        canvas.finish();
        state = 0;
    }
    
    private int addedCount = 0;
    private int moveCountInt;
    public void addShape(OutputShape shape)
    {

        Layer layer = this.layers.get(shape.layer);
        if (layer == null)
        {
            layer = addLayer(new Layer(new TreeMap<Float, List<OutputShape>>(), shape.layer));
        }
        Float width = getMetric(shape);
        List<OutputShape> list = layer.points.get(width);
        if (list == null)
        {
            list = new ArrayList<OutputShape>(2);
            layer.points.put(width, (ArrayList<OutputShape>)list);
        }

        list.add(shape);
        addedCount++;

        //state&5 == 1
        if (state < 5 && (count++ > moveCountInt))
        {
            try
            {
                moveToTmpFiles();
                count = 0;
            } 
            catch(Exception mm)
            {
                mm.printStackTrace();
            }
        }
    }
    private int addedCount2 = 0;
    private void addShape2(OutputShape shape)
    {
        Layer layer = this.layers.get(shape.layer);
        if (layer == null)
        {
            layer = addLayer(new Layer(new TreeMap<Float, List<OutputShape>>(), shape.layer));
        }
        Float width = getMetric(shape);
        List<OutputShape> list = layer.points.get(width);
        if (list == null)
        {
            list = new ArrayList<OutputShape>(2);
            layer.points.put(width, (ArrayList<OutputShape>)list);
        }

        list.add(shape);
        addedCount2++;
    }

    public int getAddedCount()
    {
        return tmpFilePhase? addedCount2 : addedCount;
    }
    
    

    protected abstract void drawRemainingShapes();
    protected abstract float getMetric(OutputShape p);
    

    protected Layer addLayer(Layer l)
    {
        int ind = Collections.binarySearch(this.layerInd, l);
        if (ind >= 0)
        {
            
        }
        else
        {
            this.layers.put(l.layerIndex, l);            
            layerInd.add(-(ind+1),l);
        }
        return l;
    }
    
    public Iterator<OutputShape> iterator()
    {
        return new Iterator<OutputShape>()
        {

            int layer = -1;
            Entry<Float, ? extends List<OutputShape>> curEntry;
            Iterator<OutputShape> curI;
            
            public boolean hasNext()
            {
                throw new UnsupportedOperationException("Not supported.");
            }
            public OutputShape next()
            {
                if (curEntry == null)
                {
                    layer++;
                    if (layer >= layerInd.size())
                        return null;
                    curEntry = layerInd.get(layer).points.firstEntry();

                }
                if (curI == null)
                {
                    curI = curEntry.getValue().iterator();
                }
                if (curI.hasNext())
                    return curI.next();
                curI = null;
                curEntry = layerInd.get(layer).points.higherEntry(curEntry.getKey());
                return next();
            }

            public void remove()
            {
                throw new UnsupportedOperationException("Not supported.");
            }
            
        };
    }


    public Iterator<OutputShape> descendingIterator()
    {
        return new Iterator<OutputShape>()
        {

            int layer = -1;
            Entry<Float, ? extends List<OutputShape>> curEntry;
            Iterator<OutputShape> curI;

            public boolean hasNext()
            {
                throw new UnsupportedOperationException("Not supported.");
            }
            public OutputShape next()
            {
                if (curEntry == null)
                {
                    layer++;
                    if (layer >= layerInd.size())
                        return null;
                    curEntry = layerInd.get(layer).points.lastEntry();
                    if (curEntry == null)
                    {
                        return next();
                    }
                }
                if (curI == null)
                {
                    curI = curEntry.getValue().iterator(); // TODO: to descending
                }
                if (curI.hasNext())
                    return curI.next();
                curI = null;
                curEntry = layerInd.get(layer).points.lowerEntry(curEntry.getKey());
                return next();
            }

            public void remove()
            {
                throw new UnsupportedOperationException("Not supported.");
            }

        };
    }



    private void moveToTmpFiles() throws IOException
    {
        int fileSize = 50000;
        if (MemoryWatch.getMemorySizeFactor() < 64000000)
            fileSize /= 4;
        if (MemoryWatch.getMemorySizeFactor() < 32000000)
            fileSize /= 2;
//        System.out.println(fileSize + " shapes per file");
        long l = 0;
        int added = 0;
        Layer layer = null;
        for (Layer ll : layerInd)
        {
            if (ll.points.size() > 0)
            {
                layer = ll;
                break;
            }
        }        
        if (layer==null)
            return;
        TmpFile f = createTmpFile(null, layer.layerIndex,
                        layer.points.lastEntry().getKey());
        Order minMetric = null;
        for (Layer ll : layerInd)
        {
            f.layermax = ll.layerIndex;
            
            Entry<Float, ? extends List<OutputShape>> e = ll.points.lastEntry();

            while (e != null)
            {
                if (f.oos == null)
                {
                    f = createTmpFile(f, ll.layerIndex,
                                e.getKey());
                }
                for(OutputShape p : e.getValue())
                {
                    try{
                    f.oos.writeObject(new Order(ll.layerIndex,e.getKey()));
                    }
                    catch(Exception ex)
                    {
                        throw new RuntimeException("disk write failed", ex);
                    }
                    f.oos.writeObject(p);
                    added++;
                }
                if(added > fileSize)
                {
                    f.oos.close();
                    f.oos = null;
                    f.min = e.getKey();
                    tmpMin.put(new Order(ll.layerIndex,e.getKey()), f);
                    e = ll.points.lowerEntry(e.getKey());
                    if (e != null)
                    {
                        f = createTmpFile(f, ll.layerIndex,
                                e.getKey());
                        
                        l += added;
                        added = 0;
                    }
                } 
                else
                {
                    e = ll.points.lowerEntry(e.getKey());
                }
                
            }   
            if (ll.points != null)
            {
                Entry<Float, ? extends List<OutputShape>> e2 = ll.points.firstEntry();
                layer = ll;
                minMetric = new Order(ll.layerIndex,e2 != null ?
                    e2.getKey() : Float.MAX_VALUE);
                if (f == null && e2 != null)
                    f = createTmpFile(f, ll.layerIndex,
                                e2.getKey());
            }
            ll.points = null;//new TreeMap<Float, LinkedList<OutputShape>>();
        }
        if (f != null)
        {
            if (f.oos != null)
            {
                f.oos.close();
                f.oos = null;
            }
            f.layermax = minMetric.layer;
            f.min = minMetric.metric;
            tmpMin.put(minMetric, f);
        }

        Runtime.sysoutln("clearing layerInd+layers", 0);
        layerInd.clear();
        //layers.clear();
        layers = new HashMap<Float, Layer>();
        l += added;
//        statString = "Moved: " + l + " shapes";
//        System.out.println(statString);
    }

    TreeMap<Order, TmpFile> tmpMin = new TreeMap<Order, TmpFile>();
    TreeMap<Order, TmpFile> tmpMax = new TreeMap<Order, TmpFile>();
    private static int idCnt = 0;
    public static class Order implements Comparable<Order>, Serializable
    {
        float layer, metric;
        int id;

        public Order(float layer, float metric)
        {
            this.layer = layer;
            this.metric = metric;
            id = idCnt++;
        }
        public String toString()
        {
            return "L" + layer + " M" + metric;
        }
        
        public int compareTo(ShapeReaderImpl.Order o)
        {
            if (layer > o.layer)
                return 1;
            if (layer < o.layer)
                return -1;
            if (metric < o.metric)
                return 1;
            if (metric > o.metric)
                return -1;
            return id-o.id;
        }

        public int compareToWeak(ShapeReaderImpl.Order o)
        {
            if (layer > o.layer)
                return 1;
            if (layer < o.layer)
                return -1;
            if (metric < o.metric)
                return 1;
            if (metric > o.metric)
                return -1;
            return 0;
        }
        
    }

    private TmpFile createTmpFile(TmpFile f0, float layerIndex, Float key) throws IOException
    {
        if (f0 != null && f0.oos != null)
            try
            {
                f0.oos.close();
                f0.oos = null;
            } 
            catch(Exception ec)
            {
                ec.printStackTrace();
            }
        TmpFile f = new TmpFile(layerIndex);
        f.max = f.min = key;
        tmpMax.put(new Order(layerIndex, key), f);
        f.openOos();
        Runtime.sysoutln("Created sr temp " + f.file, 2);
        return f;
    }
    
    TmpFile search;
    { try { search = new TmpFile(0f); } catch(Exception eos){}}
    
    private int tmpInd = 0;
    private class TmpFile
    {
        float min, max, layermin, layermax;
        File file;
        ObjectOutputStream oos;
        ObjectInputStream ois;
        
        Order current;

        TmpFile(float layer) throws IOException
        {
            this.min = Float.MIN_VALUE;
            this.max = Float.MAX_VALUE;
            this.layermin = layermax = layer;
            file = File.createTempFile(String.format("shapesL%.2f.%d", layer, tmpInd++), ".tmp");
        }

        void openOos() throws IOException
        {
            oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
        } 
        
        private ObjectInputStream openTmpFile()
        {
            Runtime.sysoutln("Opening tmp file " + file,0);
            try {
                this.ois = 
                        new ObjectInputStream(
                        new BufferedInputStream(
                        new FileInputStream(file)));
            }
            catch(Exception e)
            {
                e.printStackTrace();
                return null;
            }
            return ois;
        }        
    
    }

    
}

