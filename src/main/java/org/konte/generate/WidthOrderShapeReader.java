package org.konte.generate;

import org.konte.generate.ShapeReaderImpl;
import org.konte.image.OutputShape;
import org.konte.model.Model;

/** @author pvto */
public class WidthOrderShapeReader extends ShapeReaderImpl {

    public WidthOrderShapeReader(Model model) {
        super(model);
    }

    protected float getMetric(OutputShape p) {
        return p.getMinWidth();
    }

    @Override
    protected void drawRemainingShapes() {
        drawFirstToLast();
    }
}
