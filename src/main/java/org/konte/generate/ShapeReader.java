package org.konte.generate;

import java.util.Iterator;
import org.konte.image.Canvas;
import org.konte.image.OutputShape;



public interface ShapeReader extends Runnable {

    public int getAddedCount();

    public int state();
    public void run();
    public void finish(int step);
    public void rewind();
    public long getShapeCount();
    public Iterator<OutputShape> iterator();
    public Iterator<OutputShape> descendingIterator();
    
    public Canvas getCanvas();
    public void setCanvas(Canvas canvas);

    public void setRuleWriter(RuleWriter aThis);

    public void setEnableLaterIteration(boolean enable);

}
