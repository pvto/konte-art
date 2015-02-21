/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.konte.model;

import java.util.ArrayList;
import java.util.List;
import org.konte.misc.Mathc3;

/**
 *
 * @author pto
 */
public class GlobalLighting  {

    private ArrayList<Light> lights;

    public GlobalLighting() {
        lights = new ArrayList<Light>();
    }
    
    public void addLight(Light light) {
        lights.add(light);
    }

    public List<Light> getLights() {
        return lights;
    }
    
    
    public void lightObject(DrawingContext point) {
        float[] color = lights.get(0).lightObject(point);
        if (lights.size() > 1) {
            for (int i=1; i<lights.size(); i++) {
                float[] tmp = lights.get(i).lightObject(point);
                color[0] += tmp[0];
                color[1] += tmp[1];
                color[2] += tmp[2];
            }
        }
        point.R = Mathc3.bounds1(color[0]);
        point.G = Mathc3.bounds1(color[1]);
        point.B = Mathc3.bounds1(color[2]);
    }

}
