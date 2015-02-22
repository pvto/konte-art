package org.konte.generate;

import org.konte.image.OutputShape;
import org.konte.model.Model;

/** @author pvto */
public class SmallnessOrderShapeReader extends WidthOrderShapeReader {

    public SmallnessOrderShapeReader(Model model) {
        super(model);
    }

    @Override
    protected float getMetric(OutputShape p) {
        return p.getMinWidth();
    }

    @Override
    protected void drawRemainingShapes() {
        drawLastToFirst();
    }    
    
}
