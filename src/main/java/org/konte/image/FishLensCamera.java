package org.konte.image;

import org.konte.misc.Point2;
import org.konte.misc.Vector3;

/**
 *
 * @author Paavo Toivanen https://github.com/pvto
 */
public class FishLensCamera extends SimpleCamera {

    public float exp = 0.5f;
    public FishLensCamera(float exp) {
        this.exp = exp;
    }
    
    @Override
    public Point2 mapTo2D(Vector3 v)
    {
        Vector3 d = cameraRotationMatrix.multiply(Vector3.sub(v, position));
        float x = d.x / d.z;
        float y = d.y / d.z;
        float alfa = (float)Math.atan2(y, x);
        float dist = (float)Math.pow(x*x + y*y, exp) / d.z;
        return new Point2((float)Math.cos(alfa) * dist, (float)Math.sin(alfa) * dist);
    }
}
