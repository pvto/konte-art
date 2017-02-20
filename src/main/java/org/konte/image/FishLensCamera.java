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
    public double exp = 5;
    public float xtr = 0f;
    public float ytr = 0f;
    public FishLensType type = FishLensType.STEREOGRAPHIC;
    
    public FishLensCamera(float f, float fishLensType, float opticalBlindSpotZ, float exp, float xtr, float ytr) {
        this.f = f;
        type = FishLensType.forFloat(fishLensType);
        opticalAxis = new Vector3(0,0,opticalBlindSpotZ);
        this.exp = exp;
        this.xtr = xtr;
        this.ytr = ytr;
    }
    
    private final Vector3 opticalAxis;
        
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
        double theta = computeLinCorrectedTheta(v, place);
        float r = 2f * (float)(f * Math.tan(theta / 2.0));
        if (exp != 1.0) { r = (float)Math.pow(r, exp); }
        return project(r, place);
    }


    
    private double computeLinCorrectedTheta(Vector3 v, Vector3 place)
    {
        Vector3 d = Vector3.sub(place, opticalAxis);
        double theta;
        double xyd = Math.sqrt(d.x*d.x + d.y*d.y);
        if (xyd < .25) {
            theta = xyd / .25 * Math.atan2(d.length(), opticalAxis.z);
        } else {
            theta = Math.atan2(d.length(), opticalAxis.z);
        }
        return theta;
    }
    
    private Point2 project(float r, Vector3 place)
    {
        double alpha;
        if (Math.abs(place.z) >= .01f) {
            alpha = Math.atan2(place.y / Math.abs(place.z), place.x / Math.abs(place.z));
        } else {
            alpha = Math.atan2(place.y, place.x);
        }
         
        return new Point2((float)Math.cos(alpha) * r + xtr, (float)Math.sin(alpha) * r - ytr);
    }
    
    public Point2 equidistant(Vector3 v)
    {
        Vector3 place = cameraRotationMatrix.multiply(Vector3.sub(v, position));
        double theta = computeLinCorrectedTheta(v, place);
        float r = (float)(f * theta);
        if (exp != 1.0) { r = (float)Math.pow(r, exp); }
        return project(r, place);
    }
    
    public Point2 equisolidAngle(Vector3 v)
    {
        Vector3 place = cameraRotationMatrix.multiply(Vector3.sub(v, position));
        double theta = computeLinCorrectedTheta(v, place);
        float r = 2f * (float)(f * Math.sin(theta / 2.0));
        if (exp != 1.0) { r = (float)Math.pow(r, exp); }
        return project(r, place);
    }
    
    public Point2 orthographic(Vector3 v)
    {
        Vector3 place = cameraRotationMatrix.multiply(Vector3.sub(v, position));
        double theta = computeLinCorrectedTheta(v, place);
        float r = 2f * (float)(f * Math.sin(theta));
        if (exp != 1.0) { r = (float)Math.pow(r, exp); }
        return project(r, place);
    }
}
