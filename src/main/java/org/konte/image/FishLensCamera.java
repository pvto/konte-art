package org.konte.image;

import org.konte.misc.Point2;
import org.konte.misc.Vector3;

/**
 *
 * @author Paavo Toivanen https://github.com/pvto
 */
public class FishLensCamera  extends SimpleCamera {

    public double f = 0.5, exp2, exp3;
    
    public FishLensCamera(float f, float exp2, float exp3) {
        this.f = f;
        this.exp2 = exp2;
        this.exp3 = exp3;
    }
    
    private final Vector3 opticalAxis = new Vector3(0,0,1f);
        
    @Override
    public Point2 mapTo2D(Vector3 v)
    {
        Vector3 place = cameraRotationMatrix.multiply(Vector3.sub(v, position));
        Vector3 d = Vector3.sub(place, opticalAxis);
        double theta = (float)Math.asin(d.length() / 20.0);
        float r = 2f * (float)(f * Math.tan(theta*10.0));
        float cosxth;
        float cosyth;

        if (place.z < 0) {
            cosxth = place.x / -place.z;
            cosyth = place.y / -place.z;
        } else if (place.z == 0) {
            cosxth = place.x * 100f;
            cosyth = place.y * 100f;
            //System.out.println(r + " " + d + " " + place + " " + theta);
        } else {
            cosxth = place.x / place.z;
            cosyth = place.y / place.z;
        }
        return new Point2(cosxth * r, cosyth * r);
    }
}
