package org.konte.image;

import org.konte.misc.Point2;
import org.konte.misc.Vector3;

/**
 *
 * @author Paavo Toivanen https://github.com/pvto
 */
public class ZPowCamera extends SimpleCamera {
    
    private final float exp,
            planarXtr, planarYtr;
    
    public ZPowCamera(float exp, float xtr, float ytr)
    {
        this.exp = exp;
        this.planarXtr = xtr;
        this.planarYtr = -ytr;
    }
    
    @Override
    public Point2 mapTo2D(Vector3 v)
    {
        Vector3 d = cameraRotationMatrix.multiply(Vector3.sub(v, position));
        float zadj = (float)Math.pow(d.z, exp);
        if (zadj >= 1f)
            return new Point2(d.x / zadj + planarXtr, d.y / zadj + planarYtr);
        float fct = 2f-zadj;
        return new Point2(d.x*fct + planarXtr, d.y*fct + planarYtr);
    }
    
}
