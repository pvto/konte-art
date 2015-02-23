
package org.konte.model;

import java.util.ArrayList;
import java.util.List;
import org.konte.parse.ParseException;
/**
 *
 * @author pvto
 */
public class FieldStrength2DColorSpace extends ColorSpaceImpl {
    private ArrayList<RGBA> transformPoints;


    public int getDimension()
    {
        return 2;
    }
    public FieldStrength2DColorSpace(String name, int id)
    {
        this.name = name;
        this.id = id;
    }
    public void setPoints(ArrayList<RGBA> points)
    {
        this.transformPoints = points;
        dists = new float[points.size()];
    }
    private float[] rgbas = new float[5];
    private float[] dists;
    public float[] getValue(float... args) throws ParseException 
    {
        float x = args[0];
        float y = args[1];
        float dist0, dist1;
        float totdist = 0.0001f;
        rgbas[0] = rgbas[1] = rgbas[2] = rgbas[3] = 0f;
        rgbas[4] = strength.evaluate();
        int i = 0;
        for (RGBA rgba : this.transformPoints)
        {
            dist0 = x - rgba.point.get(0).evaluate();
            dist1 = y - rgba.point.get(1).evaluate();
            dists[i] =  1f /
                    (0.1f + (float)Math.sqrt(dist0*dist0 + dist1*dist1) /
                    rgba.fieldStrength.evaluate());
            totdist += dists[i];
            i++;
        }        
        i = 0;
        for (RGBA rgba : this.transformPoints)
        {
            dist0 = dists[i] / totdist;
            rgbas[0] += rgba.R.evaluate() * dist0;
            rgbas[1] += rgba.G.evaluate() * dist0;
            rgbas[2] += rgba.B.evaluate() * dist0;
            rgbas[3] += rgba.A.evaluate() * dist0;
            i++;
        }
        return rgbas;
    }


    public float[][] getBounds() throws ParseException 
    {
        float xmin = 0f, xmax = 0f, ymin = 0f, ymax = 0f;
        for(RGBA rgba : transformPoints)
        {
            float x = rgba.point.get(0).evaluate();
            if (x < xmin)
                xmin = x;
            if (x > xmax)
                xmax = x;
            float y = rgba.point.get(1).evaluate();
            if (y < ymin)
                ymin = y;
            if (y > ymax)
                ymax = y;
        }
        return new float[][] {
            new float[] { xmin,ymin,0 },
            new float[] { xmax,ymax,0 }
        };
    }

    public List<RGBA> getPivots()
    {
        return transformPoints;
    }
    
}
