
package org.konte.model;

import java.awt.Color;
import org.konte.lang.Language;
import org.konte.misc.Mathc3;
import org.konte.misc.Matrix4;
import org.konte.parse.ParseException;

/**
 *
 * @author pvto
 */
public class Background {

    private float r,g,b,a = 1f;
    private Color c;

    public Background() { }
    public Background(float r_, float g_, float b_, float a_)
    {
        this.r = r_;
        this.g = g_;
        this.b = b_;
        this.a = a_;
    }

    public void initFromTransform(Transform p)
    {
        DrawingContext base = new DrawingContext();
        DrawingContext tmp = null;
        base.matrix = Matrix4.IDENTITY;
        try
        {
            tmp = p.transform(base);
        }
        catch(ParseException e)
        {
            e.printStackTrace();
        }
        if (p.hasTransformType(TransformModifier.RGB.class) |
                p.hasTransformType(TransformModifier.H.class) |
                p.hasTransformType(TransformModifier.Sat.class) |
                p.hasTransformType(TransformModifier.L.class) |
                p.hasTransformType(TransformModifier.R.class) |
                p.hasTransformType(TransformModifier.G.class) |
                p.hasTransformType(TransformModifier.B.class))
        {
            r = tmp.getR();
            g = tmp.getG();
            b = tmp.getB();
        }
        else
        {
            r = g = b = 1f;
        }
        a = tmp.getA();
        if (p.hasTransformType(TransformModifier.A.class))
        {
            a = 0f;
            for(TransformModifier m : p.acqExps)
            {
                if (m.token == Language.A)
                    try
                    {
                        a += m.exprs.get(0).evaluate();
                    }
                    catch (ParseException ex) {}
            }
            a = Mathc3.bounds1(a);
        }
        else
        {
            a = 1f;
        }
        c = new Color(r,g,b,a);
    }
    
    public Color getColor()
    {
        if (c == null)
            c = new Color(r,g,b,a);
        return c;
    }
}
