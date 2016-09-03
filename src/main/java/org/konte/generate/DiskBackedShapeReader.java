package org.konte.generate;

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
import org.konte.misc.DiskBackedTreeMapBag;
import org.konte.misc.DiskBackedTreeMapBag.BagWrapper;
import org.konte.model.Model;

/**
 * @author pvto https://github.com/pvto
 */
public class DiskBackedShapeReader implements ShapeReader {

    Layers layers = new Layers();
    private RuleWriter rulew;
    private Canvas canvas;
    
    private Model model;
    private boolean enableLaterIteration;
    
    public DiskBackedShapeReader(Model model, PointMetric metric)
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
        canvas.finish();
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
        for (int i = 0; i < keyset.length; i++)
        {
            Layer layer = layers.layers.get(keyset[i]);
            canvas.initLayer(model, layer.layerIndex);
            
            Iterator lr = layer.points.descendingMap().values().iterator();
            
            while(lr.hasNext() && state == 3)
            {
                Object o = lr.next();
                if (o instanceof OutputShape)
                {
                    OutputShape p = (OutputShape)o;
                    p.shape.draw(model.cameras.get(p.fov), canvas, p);
                    shapeCount++;
                }
                else
                {
                    BagWrapper w = (BagWrapper)o;
                    BagWrapper orig = w;
                    while(w != null)
                    {
                        OutputShape p = (OutputShape)w.getValue();
                        p.shape.draw(model.cameras.get(p.fov), canvas, p);
                        shapeCount++;
                        w = w.next;
                    }
                    if (!enableLaterIteration)
                    {
                        orig.val = null; 
                        orig.next = orig.last = null; // cleanup to save memory
                    }
                }
            }
            if (state == 3)
            {
                canvas.applyEffects(model, keyset[i]);
            }
            if (!enableLaterIteration)
            {
                layer.points.freeDiskCache();
                layers.layers.put(keyset[i], null); //cleanup to save memory
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
                DiskBackedTreeMapBag map = new DiskBackedTreeMapBag(outputShapeSerializer);
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
            final Float[] keyset = layers.keySet().toArray(new Float[0]);
            Arrays.sort(keyset);
            return new Iterator<OutputShape>()
            {
                private int i = 0;
                private Iterator<Object> lr = lriter();
                private Object li = null;
                
                @Override
                public boolean hasNext()
                {
                    return li != null || lr != null && lr.hasNext() || i < keyset.length - 1;
                }
                
                private Object lrnext()
                {
                    try {
                        return lr.next();
                    }
                    catch (Exception e) {
                        return null;
                    }
                }
                
                private Iterator lriter()
                {
                    Collection vals = layers.get(keyset[i++]).points.values();
                    
                    if (desc)
                    {
                        if (!(vals instanceof List))
                        {
                            vals = new ArrayList(vals);
                        }
                        Collections.reverse((List)vals);
                    }
                    return vals.iterator();
                }

                @Override
                public OutputShape next()
                {
                    if (lr == null) return null;
                    if (li instanceof BagWrapper)
                    {
                        li = ((BagWrapper)li).next;
                        if (li == null)
                        {
                            li = lrnext();
                        }
                    }
                    else if (li instanceof OutputShape)
                    {
                        li = lrnext();
                    }
                    else if (li == null)
                    {
                        li = lrnext();
                    }
                    if (li == null)
                    {
                        if (i >= keyset.length)
                        {
                            lr = null;
                            return null;
                        }
                        lr = lriter();
                        return next();
                    }
                    if (li instanceof OutputShape)
                    {
                        return (OutputShape)li;
                    }
                    else
                    {
                        try
                        {
                            return (OutputShape)((BagWrapper)li).getValue();
                        }
                        catch(IOException e)
                        {
                            throw new RuntimeException(e);
                        }
                    }
                }

                @Override public void remove() { throw new UnsupportedOperationException("Not supported."); }
                
            };
        }
    }
    protected class Layer implements Comparable<Layer>
    {

        DiskBackedTreeMapBag points;
        float layerIndex;

        public Layer(DiskBackedTreeMapBag points, float layerIndex)
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
