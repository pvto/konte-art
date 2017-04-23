package org.konte.model;

import org.konte.model.systems.Curl2DSystem;
import org.konte.model.systems.ImgChannelSystem;
import org.konte.model.systems.Particle2DSystem;
import org.konte.model.systems.PerlinNoise2DSystem;
import org.konte.model.systems.PerlinNoise3DSystem;

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
        Perlin2(new PerlinNoise2DSystem()),
        Perlin3(new PerlinNoise3DSystem()),
        Curl2(new Curl2DSystem()),
        Particle2(new Particle2DSystem())
        ;
        
        public GreyBoxSystem generator;
        private Names(GreyBoxSystem generator) {
            this.generator = generator;
        }
    }
}
