package org.konte.generate;

import org.konte.image.OutputShape;
import org.konte.model.Model;

public interface PointMetric {
    
    float measure(OutputShape p);
    
    
    
    
    
    public static class ZMetric implements PointMetric {
        private Model model;
        public ZMetric(Model model) { this.model = model; }
        
        public float measure(OutputShape p)
        {
            return model.cameras.get(p.fov).distMetric(p.matrix);
        }
    }
    
    
    public static class MinWidthMetric implements PointMetric {
        private Model model;
        public MinWidthMetric(Model model) { this.model = model; }
        
        public float measure(OutputShape p)
        {
            return p.getMinWidth();
        }
    }
    
    
    public static class MaxWidthMetric implements PointMetric {
        private Model model;
        public MaxWidthMetric(Model model) { this.model = model; }
        
        public float measure(OutputShape p)
        {
            return - p.getMinWidth();
        }
    }    
}
