package org.konte.generate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.konte.image.Canvas;
import org.konte.image.OutputShape;
import org.konte.lang.Language;
import org.konte.misc.DiskBackedTreeMapBag;
import org.konte.misc.DiskBackedTreeMapBag.BagWrapper;
import org.konte.misc.Matrix4;
import org.konte.model.Model;

/**
 * @author pvto https://github.com/pvto
 */
public class DiskBackedShapeReader implements ShapeReader {

    Layers layers = new Layers();
    private RuleWriter rulew;
    private Canvas canvas;
    
    private Model model;
    
    public DiskBackedShapeReader(Model model, PointMetric metric)
    {
        this.model = model;
        this.metric = metric;
    }
    
    private PointMetric metric;


    public interface PointMetric { float measure(OutputShape p); }
    public static class ZMetric implements PointMetric {
        private Model model;
        public ZMetric(Model model) { this.model = model; }
        public float measure(OutputShape p) { return model.cameras.get(p.fov).distMetric(p.matrix); }
    }
    public static class MinWidthMetric implements PointMetric {
        private Model model;
        public MinWidthMetric(Model model) { this.model = model; }
        public float measure(OutputShape p) { return p.getMinWidth(); }
    }
    public static class MaxWidthMetric implements PointMetric {
        private Model model;
        public MaxWidthMetric(Model model) { this.model = model; }
        public float measure(OutputShape p) { return - p.getMinWidth(); }
    }

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
        } catch(IOException e)
        {
            e.printStackTrace();
            state = 5;
        }
    }
    private void runInternal() throws IOException {
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
                    orig.val = null;  orig.next = orig.last = null; // cleanup to save memory
                }
            }
            if (state == 3)
            {
                canvas.applyEffects(model, keyset[i]);
            }
            layer.points.freeDiskCache();
            layers.layers.put(keyset[i], null); //cleanup to save memory
        }
        //Runtime.sysoutln("drawn: " + (shapeCount-then) + " state=" + state, 0);
    }
    @Override public Iterator<OutputShape> iterator() { return layers.shapeIterator(); }
    @Override public Iterator<OutputShape> descendingIterator() { throw new UnsupportedOperationException("Not supported"); }
    @Override public Canvas getCanvas() { return canvas; }
    @Override public void setCanvas(Canvas canvas) { this.canvas = canvas; }
    @Override public void setRuleWriter(RuleWriter aThis) { this.rulew = aThis; }

    
    private static class OutputShapeSerializer implements DiskBackedTreeMapBag.Serializer {

        @Override
        public byte[] marshal(Object o) throws IOException
        {
            OutputShape p = (OutputShape)o;
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(ba);
            
            out.writeFloat(p.matrix.m00);  out.writeFloat(p.matrix.m01);  out.writeFloat(p.matrix.m02);  out.writeFloat(p.matrix.m03);
            out.writeFloat(p.matrix.m10);  out.writeFloat(p.matrix.m11);  out.writeFloat(p.matrix.m12);  out.writeFloat(p.matrix.m13);
            out.writeFloat(p.matrix.m20);  out.writeFloat(p.matrix.m21);  out.writeFloat(p.matrix.m22);  out.writeFloat(p.matrix.m23);
            out.writeFloat(p.matrix.m33);
            out.writeInt(p.col);
            out.writeFloat(p.layer);
            out.writeShort(p.fov);
            out.writeInt(p.shape.getId());
            
            return ba.toByteArray();
        }

        @Override
        public Object unmarshal(byte[] bytes) throws IOException
        {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
            OutputShape p = new OutputShape();
            p.matrix = new Matrix4();
            p.matrix.m00 = in.readFloat(); p.matrix.m01 = in.readFloat(); p.matrix.m02 = in.readFloat(); p.matrix.m03 = in.readFloat();
            p.matrix.m10 = in.readFloat(); p.matrix.m11 = in.readFloat(); p.matrix.m12 = in.readFloat(); p.matrix.m13 = in.readFloat();
            p.matrix.m20 = in.readFloat(); p.matrix.m21 = in.readFloat(); p.matrix.m22 = in.readFloat(); p.matrix.m23 = in.readFloat();
            p.matrix.m33 = in.readFloat();
            p.col = in.readInt();
            p.layer = in.readFloat();
            p.fov = in.readShort();
            p.shape = Language.getUntransformable(in.readInt());
            return p;
        }

        @Override
        public int objectSize(Object o)
        {
            return 4*4*3+4 + 4 + 4 + 2 + 4;
        }
        
    }
    
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
        public Iterator<OutputShape> shapeIterator()
        {
            final Float[] keyset = layers.keySet().toArray(new Float[0]);
            Arrays.sort(keyset);
            return new Iterator<OutputShape>()
            {
                private int i = 0;
                private Iterator<Object> lr = layers.get(keyset[i++]).points.values().iterator();
                private Object li = lr.next();
                
                @Override
                public boolean hasNext()
                {
                    return li != null || lr.hasNext() || i < keyset.length - 1;
                }

                @Override
                public OutputShape next()
                {
                    if (lr == null) return null;
                    if (li == null)
                    {
                        li = lr.next();
                        if (li == null)
                        {
                            if (i == keyset.length)
                            {
                                lr = null;
                                return null;
                            }
                            lr = layers.get(keyset[i++]).points.values().iterator();
                            if (lr == null) return null;
                            li = lr.next();
                        }
                    }
                    OutputShape p;
                    if (li instanceof OutputShape)
                    {
                        p = (OutputShape)li;
                    }
                    else
                    {
                        try
                        {
                            p = (OutputShape)((BagWrapper)li).getValue();
                        }
                        catch(IOException e)
                        {
                            throw new RuntimeException(e);
                        }
                    }
                    
                    return p;
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
