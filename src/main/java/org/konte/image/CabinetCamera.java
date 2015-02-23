package org.konte.image;

import org.konte.misc.Point2;
import org.konte.misc.Vector3;

/**
 * @author pvto https://github.com/pvto
 */
public class CabinetCamera extends SimpleCamera {

    public final float alpha;
    private final float cosAlpha;
    private final float sinAlpha;
    private final float zContraction;
    
    public CabinetCamera(float alpha, float zContraction)
    {
        this.alpha = alpha;
        this.cosAlpha = (float)Math.cos(alpha);
        this.sinAlpha = -(float)Math.sin(alpha);
        this.zContraction = zContraction;
    }

    
    @Override
    public Point2 mapTo2D(Vector3 v)
    {
        Vector3 d = Vector3.sub(v, position);
        d = cameraRotationMatrix.multiply(d);
        
        return new Point2(
                d.x + zContraction * d.z * cosAlpha, 
                d.y + zContraction * d.z * sinAlpha
        );
    }
}
