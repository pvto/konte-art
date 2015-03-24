
package org.konte.model;

import java.util.ArrayList;
import java.util.List;
import org.konte.misc.Mathc3;

/**
 *
 * @author pvto
 */
public class GlobalLighting  {

    private final ArrayList<Light> lights;

    public GlobalLighting()
    {
        lights = new ArrayList<>();
    }
    
    public void addLight(Light light)
    {
        if (light instanceof AmbientLight)
        {
            if (lights.size() > 0 && lights.get(0) instanceof AmbientLight)
                lights.set(0, light);
            else
                lights.add(0, light);
        }
        else
        {
            lights.add(light);
        }
        
        if (light instanceof PhongLight && !(lights.get(0) instanceof AmbientLight))
        {
            light = new AmbientLight();
            lights.add(0, light);
        }
        
    }

    public List<Light> getLights()
    {
        return lights;
    }
    
    
    public void lightObject(DrawingContext point)
    {
        float[] color = lights.get(0).lightObject(point);
        if (lights.size() > 1)
        {
            for (int i=1; i<lights.size(); i++)
            {
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
