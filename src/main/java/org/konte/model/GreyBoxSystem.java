package org.konte.model;

import org.konte.model.systems.ImgChannelSystem;

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
        Channel(new ImgChannelSystem())
        ;
        
        public GreyBoxSystem generator;
        private Names(GreyBoxSystem generator) {
            this.generator = generator;
        }
    }
}
