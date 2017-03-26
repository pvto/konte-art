package org.konte.model;

import org.konte.model.systems.ImgChannelSystem;
import org.konte.model.systems.PerlinNoise2DSystem;

/**
 *
 * @author Paavo Toivanen https://github.com/pvto
 */
public interface GreyBoxSystem {
    
    GreyBoxSystem newInstance();
    void  initialize(Object[] args);
    void  evaluate(float[] args);
    float read(float[] args);
    void  write(float[] args);

    
    public static enum Names {
        Channel(new ImgChannelSystem()),
        Perlin2(new PerlinNoise2DSystem())
        ;
        
        public GreyBoxSystem generator;
        private Names(GreyBoxSystem generator) {
            this.generator = generator;
        }
    }
}
