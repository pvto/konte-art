package org.konte.image;

import org.konte.misc.Point2;
import org.konte.misc.Vector3;

/**
 *
 * @author Paavo Toivanen https://github.com/pvto
 */
public class FishCamera extends SimpleCamera {

    public double exp = 0.5;
    public double exp2 = 1;
    public double exp3 = 2;
    
    public FishCamera(float exp, float exp2, float exp3) {
        this.exp = exp;
        this.exp2 = exp2;
        this.exp3 = exp3;
    }
    
    @Override
    public Point2 mapTo2D(Vector3 v)
    {
        Vector3 d = cameraRotationMatrix.multiply(Vector3.sub(v, position));
        float x = d.x / d.z;
        float y = d.y / d.z;
        float alfa = (float)Math.atan2(y, x);
        float dist = (float)Math.pow(Math.pow(x*x + y*y, exp) / Math.pow(Math.max(0.1, d.z), exp2), exp3);
        return new Point2((float)Math.cos(alfa) * dist, (float)Math.sin(alfa) * dist);
    }
}
