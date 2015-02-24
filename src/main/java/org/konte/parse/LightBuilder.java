
package org.konte.parse;

import java.util.ArrayList;
import org.konte.expression.Expression;
import org.konte.expression.Value;
import org.konte.model.Light;
import org.konte.model.ColorSpace.RGBA;
import org.konte.model.DrawingContext;
import org.konte.model.Transform;
import org.konte.model.TransformModifier;

/**
 *
 * @author pvto
 */
public class LightBuilder {

    void type(int i)
    {
        type = i;
    }

    private Light createDefault(final Expression x, 
                final Expression y, final Expression z, final Expression strgth, 
                final RGBA rgba)
                {
        return new Light()
        {

            public float[] lightObject(DrawingContext shape)
            {
//                Name.gene.current = shape;
                float dirMult = 1f;
                float strength = 1f;
                float dist = 1f;
                try {
                    float xs = (x.evaluate()-shape.matrix.m03);
                    float ys = (y.evaluate()-shape.matrix.m13);
                    float zs = (z.evaluate()-shape.matrix.m23);
                    dist = (float)Math.sqrt(xs*xs+ys*ys+zs*zs);
                    strength = strgth.evaluate()*dirMult;
                    if (strength < 0f)
                        strength = 0f;                    
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }

                float red = shape.getR();
                float green = shape.getG();
                float blue = shape.getB();

                float factor = (dist>0f) ? 
                    (float)Math.log1p(strength/(dist*dist)) : 100f;
                try {
                    red = red*rgba.R.evaluate()*factor;
                    green = green*rgba.G.evaluate()*factor;
                    blue = blue*rgba.B.evaluate()*factor;
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }                
                if (red > 1f) red = 1f;
                if (green > 1f) green = 1f;
                if (blue > 1f) blue = 1f;
                if (red < 0f) red = 0f;
                if (green < 0f) green = 0f;
                if (blue < 0f) blue = 0f;
                return new float[] {red,green,blue,shape.getA()};
            }
            
        };        
    }
    private Light createOpposite(final Expression x, 
                final Expression y, final Expression z, final Expression strgth, 
                final RGBA rgba)
                {
        return new Light()
        {

            public float[] lightObject(DrawingContext shape)
            {
//                Name.gene.current = shape;
                float dirMult = 1f;
                float strength = 1f;
                float dist = 1f;
                try {
                    float xs = (x.evaluate()-shape.matrix.m03);
                    float ys = (y.evaluate()-shape.matrix.m13);
                    float zs = (z.evaluate()-shape.matrix.m23);
                    dist = (float)Math.sqrt(xs*xs+ys*ys+zs*zs);
                    strength = strgth.evaluate()*dirMult;
                
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }

                float red = shape.getR();
                float green = shape.getG();
                float blue = shape.getB();

                float factor = (dist>0f) ? 
                    -(float)Math.log1p(strength/(dist*dist)) : -100f;
                try {
                    red = red*rgba.R.evaluate()*factor;
                    green = green*rgba.G.evaluate()*factor;
                    blue = blue*rgba.B.evaluate()*factor;
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }                

                return new float[] {red,green,blue,shape.getA()};
            }  
        };   
    }

    private Light createDarkness(final Expression x, 
                final Expression y, final Expression z, final Expression strgth, 
                final RGBA rgba)
                {
        return new Light()
        {

            public float[] lightObject(DrawingContext shape)
            {
//                Name.gene.current = shape;
                float dirMult = 1f;
                float strength = 1f;
                float dist = 1f;
                try {
                    float xs = (x.evaluate()-shape.matrix.m03);
                    float ys = (y.evaluate()-shape.matrix.m13);
                    float zs = (z.evaluate()-shape.matrix.m23);
                    dist = (float)Math.sqrt(xs*xs+ys*ys+zs*zs);
                    strength = strgth.evaluate()*dirMult;
                
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }

                float red = 0f;
                float green = 0f;
                float blue = 0f;

                float factor = (dist>0f) ? 
                    -(float)Math.log1p(strength/(dist*dist)) : -100f;
                try {
                    red = rgba.R.evaluate()*factor;
                    green = rgba.G.evaluate()*factor;
                    blue = rgba.B.evaluate()*factor;
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                    //throw new ParseException("Can't evaluate light expression: " + e.getMessage());
                }                

                return new float[] {red,green,blue,shape.getA()};
            }  
        };
    }
    
    
    
    private String name;
    private Expression strength;
    private ArrayList<RGBA> points = null;
    private RGBA current = null;    
    private int type =0;

    
    public Light build() throws Exception 
    {
        Light light = null;
        int i = 0;
        Expression x = points.get(i).point.get(0);
        Expression y = points.get(i).point.get(1);
        Expression z = points.get(i).point.get(2);
        RGBA rgba = points.get(i);
        strength = strength == null ? new Value(1f) : strength;
        
        //final Color c = new Color(pos.getR(),pos.getG(),pos.getB(),pos.getA());
        
        if (type==0)
        {
            light = createDefault(x,y,z,strength,rgba);
        } else if (type==1)
        {
            light = createOpposite(x,y,z,strength,rgba);
        } else if (type==2)
        {
            light = createDarkness(x,y,z,strength,rgba);
        }



        return light;
    }

    void clearPointData()
    {
        type = 0;
        if (points == null)
            points = new ArrayList<RGBA>();
        else points.clear();
    }

    void name(String s)
    {
        name = s;
    }

    void point(ArrayList<Expression> coords, Transform colors) throws ParseException 
    {

        if (coords.size() != 3)
            throw new ParseException("Light position must have three coordinates");
        current = new RGBA();
        for (TransformModifier exp : colors.acqExps)
        {
            if (exp instanceof TransformModifier.RGB)
            {
                current.R = exp.exprs.get(0);
                current.G = exp.exprs.get(1);
                current.B = exp.exprs.get(2);
            } else
                throw new ParseException("Misplaced transform in light (use RGB only): " + exp);
        }
        for (TransformModifier exp : colors.acqTrs)
        {
            if (exp instanceof TransformModifier.s)
            {
                current.fieldStrength = exp.exprs.get(0);
            } else 
                throw new ParseException("Misplaced transform in shading(use s only to set field strength: " + exp);
        }
        if (current.fieldStrength == null)
            current.fieldStrength = new Value(1f);
        if (!colors.hasTransformType(TransformModifier.RGB.class))
        {
            current.R = current.G = current.B = Value.ZERO;
        } 
        current.point = new ArrayList<Expression>(coords);
        
        points.add(current); 
        if (points.size() > 1)
            throw new ParseException("Only one point in light supported");
    }

    void strength(Expression lexpr)
    {
        strength = lexpr;
    }

}
