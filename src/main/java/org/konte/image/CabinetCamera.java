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
    
    public CabinetCamera(float alpha)
    {
        this.alpha = alpha;
        this.cosAlpha = (float)Math.cos(alpha);
        this.sinAlpha = -(float)Math.sin(alpha);
    }

    
    @Override
    public Point2 mapTo2D(Vector3 v)
    {
        Vector3 d = Vector3.sub(v, position);
        d = cameraRotationMatrix.multiply(d);
        
        return new Point2(
                d.x + 0.5f * d.z * cosAlpha, 
                d.y + 0.5f * d.z * sinAlpha
        );
    }
}
