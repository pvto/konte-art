/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.konte.model;

import java.util.ArrayList;
import java.util.List;
import org.konte.expression.Expression;
import org.konte.parse.ParseException;

/**
 *
 * @author pto
 */
public interface ColorSpace {

    public float[][] getBounds() throws ParseException;

    public int getId();
    public String getName();
    public int getDimension();

    public List<RGBA> getPivots();

    
    /**How opaque a shading from this colorspace is: 0 is perfectly transparent,
     * 1 is fully opaque.
     * 
     * @return an opaqueness strength value between 0 and 1
     */
    public void setStrength(Expression strength); // 
    /**Return a RGBAS value for the given coordinates (x,y,z), in that order.
     * 
     * @param args an array of coordinates. One dimensional color spaces
     * will use only the first cell in the array, two dimensional the first
     * and the second, and three dimensional all three.
     * @return a float arrya containing RGBAS info. S is the opaqueness over current
     * RGBA with which this shading is applied.
     * @throws org.konte.parse.ParseException
     */
    public float[] getValue(float... args) throws ParseException;
    
    public static class RGBA {
        public Expression R,G,B,A;
        public ArrayList<Expression> point;
        public Expression fieldStrength;
        public String toString() { return String.format("R %s G %s B %s A %s -- %s", R,G,B,A,point); }
    }    
}
