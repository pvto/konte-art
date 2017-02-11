package org.konte.image;

import org.konte.misc.Point2;
import org.konte.misc.Vector3;

/**
 *
 * @author Paavo Toivanen https://github.com/pvto
 */
public class FishLensCamera  extends SimpleCamera {

    public static enum FishLensType {
        STEREOGRAPHIC(0),
        EQUIDISTANT(1),
        EQUISOLID_ANGLE(2),
        ORTHOGRAPHIC(3);
        final int x;
        private FishLensType(int x) { this.x = x; }
        public static FishLensType forFloat(float x) 
        {
            int i = (int)Math.round(x);
            for(FishLensType ft : values())
                if (ft.x == i)
                    return ft;
            return STEREOGRAPHIC;
        }
    }
    public double f = 0.5;
    public FishLensType type = FishLensType.STEREOGRAPHIC;
    
    public FishLensCamera(float f, float fishLensType) {
        this.f = f;
        type = FishLensType.forFloat(fishLensType);
    }
    
    private final Vector3 opticalAxis = new Vector3(0,0,1f);
        
    @Override
    public Point2 mapTo2D(Vector3 v)
    {
        switch(type) 
        {
            case EQUIDISTANT: return equidistant(v);
            case EQUISOLID_ANGLE: return equisolidAngle(v);
            case ORTHOGRAPHIC: return orthographic(v);
            default:
            case STEREOGRAPHIC: return stereographic(v);
        }
    }
    
    public Point2 stereographic(Vector3 v)
    {
        Vector3 place = cameraRotationMatrix.multiply(Vector3.sub(v, position));
        Vector3 d = Vector3.sub(place, opticalAxis);
        double theta = (float)Math.atan2(d.length(), opticalAxis.z);
        float r = 2f * (float)(f * Math.tan(theta / 2.0));
        return project(r, place);
    }

    final float halfPi = (float)(Math.PI / 2.0);
    private Point2 project(float r, Vector3 place)
    {
        float xth = (float)Math.atan2(place.x, place.z);
        float yth = (float)Math.atan2(place.y, place.z);
        return new Point2((float)Math.sin(xth) * r, (float)Math.sin(yth) * r);
/*        float cosxth;
        float cosyth;

        if (place.z < 0) {
            cosxth = place.x / -place.z;
            cosyth = place.y / -place.z;
        } else if (place.z == 0) {
            cosxth = place.x * 100f;
            cosyth = place.y * 100f;
        } else {
            cosxth = place.x / place.z;
            cosyth = place.y / place.z;
        }
        return new Point2(tanxth * r, tanyth * r);*/
    }
    
    public Point2 equidistant(Vector3 v)
    {
        Vector3 place = cameraRotationMatrix.multiply(Vector3.sub(v, position));
        Vector3 d = Vector3.sub(place, opticalAxis);
        double theta = (float)Math.atan2(d.length(), opticalAxis.z);
        float r = (float)(f * theta);
        return project(r, place);
    }
    
    public Point2 equisolidAngle(Vector3 v)
    {
        Vector3 place = cameraRotationMatrix.multiply(Vector3.sub(v, position));
        Vector3 d = Vector3.sub(place, opticalAxis);
        double theta = (float)Math.atan2(d.length(), opticalAxis.z);
        float r = 2f * (float)(f * Math.sin(theta / 2.0));
        return project(r, place);
    }
    
    public Point2 orthographic(Vector3 v)
    {
        Vector3 place = cameraRotationMatrix.multiply(Vector3.sub(v, position));
        Vector3 d = Vector3.sub(place, opticalAxis);
        double theta = (float)Math.atan2(d.length(), opticalAxis.z);
        float r = 2f * (float)(f * Math.sin(theta));
        return project(r, place);
    }
}
