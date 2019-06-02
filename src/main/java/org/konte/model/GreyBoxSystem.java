package org.konte.model;

import org.konte.model.systems.*;

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
        Particle2(new Particle2DSystem()),
        Particle3(new Particle3DSystem()),
        Worley2(new WorleyNoise2DSystem()),
        Triangulation(new TriangulationSystem())
        ;

        public GreyBoxSystem generator;
        private Names(GreyBoxSystem generator) {
            this.generator = generator;
        }
    }
}
