package org.konte.model;

import org.konte.expression.Expression;
import org.konte.expression.Value;
import org.konte.misc.Mathc3;
import org.konte.parse.ParseException;

/**
 * @author pvto https://github.com/pvto
 */
public class AmbientLight implements Light {

    public Expression ambient = new Value(0.1f);
    public int ambientId = -1;
    
    
    @Override
    public float[] lightObject(DrawingContext point) {
        float ka = Mathc3.bounds1(point.getDef(this.ambientId, 1f));
        
        try {
            float r = ka * ambient.evaluate() * point.getR();
            float g = ka * ambient.evaluate() * point.getG();
            float b = ka * ambient.evaluate() * point.getB();
            return new float[]{r,g,b, point.getA()};
        }
        catch(ParseException e) {
            e.printStackTrace();
        }
        return new float[]{0,0,0,point.getA()};
    }

}
