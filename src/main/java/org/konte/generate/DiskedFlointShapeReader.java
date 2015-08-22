package org.konte.generate;

import static org.konte.misc.FlointTree.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.konte.image.Canvas;
import org.konte.image.OutputShape;
import org.konte.misc.DiskBackedFlointTree;
import org.konte.misc.DiskBackedTreeMapBag;
import org.konte.misc.FlointTree;
import org.konte.model.Model;

public class DiskedFlointShapeReader implements ShapeReader {

    Layers layers = new Layers();
    private RuleWriter rulew;
    private Canvas canvas;
    
    private Model model;
    private boolean enableLaterIteration;
    
    public DiskedFlointShapeReader(Model model, PointMetric metric)
    {
        this.model = model;
        this.metric = metric;
    }
    
    @Override
    public void setEnableLaterIteration(boolean enable)
    {
        this.enableLaterIteration = enable;
    }

    private PointMetric metric;




    @Override public int getAddedCount() { return layers.addedCount; }
    public transient int state = 0;
    @Override public int state() { return state; }

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

    @Override
    public void run()
    {
        try {
            runInternal();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            state = 5;
        }
    }
    private void runInternal() throws IOException 
    {
        rulew.countdown.countDown();
        state = 1;
        Throwable t = null;
        for(;;)
        {
            try
            {
                List<OutputShape> shapes = rulew.exchangeShapes();
                addShapes(shapes);

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
            layers.addPoint(p);
            if (state > 2)
            {
                Runtime.sysoutln("sr1.1 " + state, 0);
                state = 0;
                return;
            }
        }
        try
        {
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
            drawAllShapes();
        }
        state = 5;
        state = 0;
    }
    
    private void addShapes(List<OutputShape> shapes)  throws Exception
    {
        int i = 0;
        for(OutputShape p : shapes)
        {
            if (i++ % 100 == 0)
                try
                {
                    p.shape.draw(model.cameras.get(p.fov), canvas, p);
                } 
                catch(Exception e)
                {
                    Runtime.sysoutln("Unable to draw shape " + p.shape.name + " ["+ p.shape.getClass() + "]", 20);
                    throw e;
                }
            layers.addPoint(p);
        }
    }

    private int shapeCount = 0;
    @Override public long getShapeCount() { return shapeCount; }

    private void drawAllShapes() throws IOException
    {

        Float[] keyset = layers.layers.keySet().toArray(new Float[0]);
        Arrays.sort(keyset);
        for (int x = 0; x < keyset.length; x++)
        {
            Layer layer = layers.layers.get(keyset[x]);
            DiskBackedFlointTree lr = layer.points;

            out: for(int i = lr.root.children.length; i >= 0; i--) { Node1 n1 = lr.root.children[i]; if (n1 != null)
                for(int j = n1.children.length; j >= 0; j--) { Node2 n2 = n1.children[j]; if (n2 != null)
                    for(int k = n2.children.length; k >= 0; k--) { Node3 n3 = n2.children[k]; if (n3 != null)
                        for(int L = n3.children.length; L >= 0; L--) { Node4 n4 = n3.children[L]; if (n4 != null)
                            for(int m = n4.children.length; m >= 0; m--) { Node5 n5 = n4.children[m]; if (n5 != null)
                                for(int n = n5.children.length; n >= 0; n--) { Node6 n6 = n5.children[n]; if (n6 != null)
                                    {
                                        FlointTree.FUPair fu = n6.firstChild;
                                        while(fu != null)
                                        {
                                            if (state != 3)
                                            {
                                                break out;
                                            }
                                            OutputShape p;
                                            if (fu.u instanceof DiskBackedFlointTree.DiskWrapper)
                                            {
                                                DiskBackedFlointTree.DiskWrapper w = (DiskBackedFlointTree.DiskWrapper)fu.u;
                                                p = (OutputShape)w.getValue();
                                                if (!enableLaterIteration)
                                                {
                                                    w.val = null; 
                                                }
                                            }
                                            else
                                            {
                                                p = (OutputShape)fu.u;
                                            }
                                            p.shape.draw(model.cameras.get(p.fov), canvas, p);
                                            shapeCount++;
                                            fu = fu.next;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            if (state == 3)
            {
                canvas.applyEffects(model, keyset[x]);
            }
            if (!enableLaterIteration)
            {
                layer.points.freeDiskCache();
                layers.layers.put(keyset[x], null); //cleanup to save memory
            }
        }
        //Runtime.sysoutln("drawn: " + (shapeCount-then) + " state=" + state, 0);
    }
    @Override public Iterator<OutputShape> iterator() { return layers.shapeIterator(false); }
    @Override public Iterator<OutputShape> descendingIterator() { return layers.shapeIterator(true); }
    @Override public Canvas getCanvas() { return canvas; }
    @Override public void setCanvas(Canvas canvas) { this.canvas = canvas; }
    @Override public void setRuleWriter(RuleWriter aThis) { this.rulew = aThis; }

    
    OutputShapeSerializer outputShapeSerializer = new OutputShapeSerializer();
    
    
    protected class Layers
    {
        public int addedCount = 0;
        Map<Float,Layer> layers = new HashMap<Float,Layer>();
        public void addPoint(OutputShape p) throws IOException
        {
            Layer layer = layers.get(p.layer);
            if (layer == null)
            {
                DiskBackedFlointTree map = new DiskBackedFlointTree(outputShapeSerializer);
                layers.put(p.layer, layer = new Layer(map, p.layer));
            }
            layer.addPoint(p);
            if (addedCount >= 2000000 && addedCount % 200000 == 0)
            {
                Runtime.sysoutln("DB-SR: flush " + addedCount, 10);
                layer.points.flushToDiskAssumeConstantSizeObjects();
            }
            addedCount++;
        }
        public Iterator<OutputShape> shapeIterator(final boolean desc)
        {
            return null;
        }
    }
    protected class Layer implements Comparable<Layer>
    {

        DiskBackedFlointTree points;
        float layerIndex;

        public Layer(DiskBackedFlointTree points, float layerIndex)
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
        
        public void addPoint(OutputShape p)
        {
            float dist = metric.measure(p);
            points.put(dist, p);
        }
        
    }
}
