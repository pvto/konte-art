package org.konte.generate;

import org.konte.image.OutputShape;
import org.konte.model.Model;

/**
 *
 * @author pvto
 */
public class ZOrderShapeReader extends ShapeReaderImpl {

    
    public ZOrderShapeReader(Model model) {
        super(model);
    }    
    
    @Override
    protected float getMetric(OutputShape p) {
        return model.cameras.get(p.fov).distMetric(p.matrix);
    }
    
    
    @Override
    protected void drawRemainingShapes() {
        drawLastToFirst();
    }

    
}
