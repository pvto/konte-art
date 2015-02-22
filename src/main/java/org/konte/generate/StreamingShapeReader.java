package org.konte.generate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.konte.expression.Expression;
import org.konte.image.Canvas;
import org.konte.image.OutputShape;
import org.konte.model.Model;

/**
 *
 * @author pvto
 */
public class StreamingShapeReader implements ShapeReader {

    private int addedCount = 0;
    private Canvas canvas;
    private RuleWriter ruleWriter;
    private Model model;
    private int state;

    public Expression streamRate = null;

    public StreamingShapeReader(Model model)
    {
        this.model = model;
    }

    public int getAddedCount()
    {
        return addedCount;
    }

    private long lastUpdtCycle;
    private int shapesDrawnInCycle = 0;
    /**This template method polls shapes from RuleWriter, and
     * either draws some immediately on the canvas, or delays
     * all drawing till RuleWriter is exhausted.
     *
     */
    @Override
    public void run()
    {
        state = 1;
        canvas.init(canvas.getWidth(), canvas.getHeight());
        lastUpdtCycle = System.currentTimeMillis();
        Float rate = null;
        float prevlayer = 0f;
        for(;;)
        {
            try
            {
                List<OutputShape> shapes;
                synchronized(ruleWriter.lock1)
                {
                    ruleWriter.lock1.wait();
                    shapes = ruleWriter.shapes;
                    ruleWriter.shapes = new ArrayList<OutputShape>();
                }
                try
                {
                    rate = streamRate.evaluate();
                }
                catch (Exception e) { }
                
                prevlayer = shapes.get(0).layer;
                for(OutputShape p : shapes)
                {
                    if (p.layer != prevlayer)
                    {
                        canvas.applyEffects(model, prevlayer);
                        prevlayer = p.layer;
                    }
                    try
                    {
                        p.shape.draw(model.cameras.get(p.fov), canvas, p);
                        addedCount++;
                    } 
                    catch(Exception e)
                    {
                        Runtime.sysoutln("Unable to draw shape " + p.shape.name + " ["+ p.shape.getClass() + "]", 20);
                        throw e;
                    }
                    if (rate != null && rate > 0f)
                    {
                        delayByStreamRate(rate);
                    }
                }
            } 
            catch(Exception e)
            {
                e.printStackTrace();
            }
            if (state > 1)
            {
                break;
            }
        }
        if (state == 2)
        {
            List<OutputShape> rest = ruleWriter.shapes;
            state = 3;
            try
            {
                rate = streamRate.evaluate();
            } catch (Exception e) { }

            for (OutputShape p : rest)
            {
                if (p.layer != prevlayer)
                {
                    canvas.applyEffects(model, prevlayer);
                    prevlayer = p.layer;
                }
                p.shape.draw(model.cameras.get(p.fov), canvas, p);
                addedCount++;
                if (state != 3)
                    break;
                if (rate != null && rate > 0f)
                {
                    delayByStreamRate(rate);
                }
            }
        }
        canvas.applyEffects(model, prevlayer);
        state = 0;
    }

    public void finish(int step)
    {
        if (state==1 || state == 3)
            state = step;
    }


    public void rewind()
    {
        shapesDrawnInCycle = 0;
        addedCount = 0;
    }

    public long getShapeCount()
    {
        return addedCount;
    }

    public Iterator<OutputShape> iterator() {
        throw new UnsupportedOperationException("Not supported yet: " + this.getClass().getName());
    }

    public Iterator<OutputShape> descendingIterator() {
        throw new UnsupportedOperationException("Not supported yet: " + this.getClass().getName());
    }

    public Canvas getCanvas()
    {
        return this.canvas;
    }

    public void setCanvas(Canvas canvas)
    {
        this.canvas = canvas;
    }

    public void setRuleWriter(RuleWriter aThis)
    {
        this.ruleWriter = aThis;
    }

    public int state()
    {
        return state;
    }


    private void delayByStreamRate(Float rate)
    {
        float estimate = ++shapesDrawnInCycle;
        float req = rate * (System.currentTimeMillis()-lastUpdtCycle) / 1000f;
        while(estimate > req)
        {
            org.konte.misc.Func.sleep(1);
            req = rate * (System.currentTimeMillis()-lastUpdtCycle) / 1000f;
        }
        if (shapesDrawnInCycle >= rate)
        {
            lastUpdtCycle = System.currentTimeMillis();
            shapesDrawnInCycle = 0;
        }
    }



}
