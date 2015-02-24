package org.konte.misc;

/**
 *
 * @author pvto
 */
public class Mathc3 {
    
    public static float bounds1(float d) {
        if ( d > 0f) {
            if (d < 1f) {
                return d;
            }
            return 1f;
        }
        return 0f;

    }

    public static float roll360(float d) {
        if (d > 0f) {
            d = d % 360f;
            if (d==0f) return 360f;
        }
        else if (d < 0f) 
        {
            d = d % 360f + 360f;
            if (d==360f) return 0f;
        }
        return d;
    }

    public static float roll1(float d) {
        if (d > 0f) {
            if (d < 1f)
                return d;
            d = d % 1f;
            if (d==0f) return 1f;
        }
        else if (d < 0f) 
        {
            if (d > -1f)
                return d;            
            d = d % 1f + 1f;
            if (d==1f) return 0f;
        }
        return d;
    }    
}
