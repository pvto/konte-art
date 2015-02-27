
package org.konte.model;

import java.util.ArrayList;
import java.util.List;
import org.konte.model.ColorSpace.RGBA;
import org.konte.parse.ParseException;
import static org.konte.misc.Mathc3.bounds1;
/**
 *
 * @author pvto
 */
public class LinearColorSpace extends ColorSpaceImpl {
    private ArrayList<RGBA> transformPoints;


    public int getDimension()
    {
        return 1;
    }
    public LinearColorSpace(String name, int id)
    {
        this.name = name;
        this.id = id;
    }
    public void setPoints(ArrayList<RGBA> points)
    {
        this.transformPoints = points;
    }
    private float[] rgbas = new float[5];
    public float[] getValue(float x) throws ParseException 
    {
        RGBA lwr = null;
        RGBA hgr = null;
        float tmpmin = -10000000f;
        float tmpmax =  10000000f;
        float tmp;
        for(RGBA rgba : transformPoints)
        {
            tmp = rgba.point.get(0).evaluate();
            if (tmp <= x && tmp > tmpmin)
            {
                lwr = rgba;
                tmpmin = tmp;
            }
            if (tmp > x && tmp < tmpmax)
            {
                hgr = rgba;
                tmpmax = tmp;
            }
        }
        if (lwr == null )
        {
            rgbas[0] = bounds1(hgr.R.evaluate());
            rgbas[1] = bounds1(hgr.G.evaluate());
            rgbas[2] = bounds1(hgr.B.evaluate());
            rgbas[3] = bounds1(hgr.A.evaluate());

        } else if (hgr == null )
        {
            rgbas[0] = bounds1(lwr.R.evaluate());
            rgbas[1] = bounds1(lwr.G.evaluate());
            rgbas[2] = bounds1(lwr.B.evaluate());
            rgbas[3] = bounds1(lwr.A.evaluate());
        }
        else
        {
            float xsize = tmpmax - tmpmin;
            if (xsize == 0)
            {
                rgbas[0] = bounds1(lwr.R.evaluate());
                rgbas[1] = bounds1(lwr.G.evaluate());
                rgbas[2] = bounds1(lwr.B.evaluate());
                rgbas[3] = bounds1(lwr.A.evaluate());
            }
            else
            {
                xsize = (x-tmpmin)/xsize;
                tmp = lwr.R.evaluate();
                rgbas[0] = bounds1(tmp + (hgr.R.evaluate() - tmp)*xsize);
                tmp = lwr.G.evaluate();
                rgbas[1] = bounds1(tmp + (hgr.G.evaluate() - tmp)*xsize);
                tmp = lwr.B.evaluate();
                rgbas[2] = bounds1(tmp + (hgr.B.evaluate() - tmp)*xsize);
                tmp = lwr.A.evaluate();
                rgbas[3] = bounds1(tmp + (hgr.A.evaluate() - tmp)*xsize);
            }
        }
        rgbas[4] = bounds1(strength.evaluate());
        return rgbas;
    }

    public float[][] getBounds() throws ParseException 
    {
        float xmin = 0f, xmax = 0f;
        for(RGBA rgba : transformPoints)
        {
            float x = rgba.point.get(0).evaluate();
            if (x < xmin)
                xmin = x;
            if (x > xmax)
                xmax = x;
        }
        return new float[][] {
            new float[] { xmin,0,0 },
            new float[] { xmax,0,0 }
        };
    }

    public List<RGBA> getPivots()
    {
        return transformPoints;
    }




    
}
