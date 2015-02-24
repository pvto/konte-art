
package org.konte.parse;

import java.util.ArrayList;
import org.konte.expression.Expression;
import org.konte.expression.Value;
import org.konte.model.ColorSpace;
import org.konte.model.FieldStrength2DColorSpace;
import org.konte.model.LinearColorSpace;
import org.konte.model.Transform;
import org.konte.model.TransformModifier;
import static org.konte.model.ColorSpace.RGBA;

/**
 *
 * @author pvto
 */
public class ColorSpaceBuilder {

    private String name = null;
    private Expression strength;
    private ArrayList<RGBA> points = null;
    private RGBA current = null;
    private int dimension = -1;    
    
    
    public ColorSpaceBuilder name(String name)
    {
        if (points == null)
            points = new ArrayList<RGBA>();
        else points.clear();
        dimension = -1;
        this.name = name; 
        return this; 
    }
    public ColorSpaceBuilder strength(Expression strength)
    {
        this.strength = strength;
        return this;
    }     
    public ColorSpaceBuilder point(ArrayList<Expression> coords, Transform colors) throws ParseException 
    {
        if (dimension != -1 && dimension != coords.size())
        {
            if (dimension < coords.size())
            {
                for (RGBA point : points)
                {
                    for (int i = 0; i < coords.size() - dimension; i++)
                        point.point.add(Value.ZERO);
                }
            }
            else
            {
                int tmp = dimension - coords.size();
                for (int i = 0; i < tmp; i++)
                    coords.add(Value.ZERO);
                dimension = coords.size();                
            }
        } else
            if (dimension < coords.size())
                dimension = coords.size();
        current = new RGBA();
        for (TransformModifier exp : colors.acqExps)
        {
            if (exp instanceof TransformModifier.RGB)
            {
                current.R = exp.exprs.get(0);
                current.G = exp.exprs.get(1);
                current.B = exp.exprs.get(2);
            } else if (exp instanceof TransformModifier.A)
            {
                current.A = exp.exprs.get(0);
            } else
                throw new ParseException("Misplaced transform in shading (use RGB and A only): " + exp);
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
        if (!colors.hasTransformType(TransformModifier.A.class)) 
            current.A = Value.ONE;
        current.point = new ArrayList<Expression>(coords);
        
        points.add(current);            
        return this; 
    }
    
    public ColorSpace build() throws Exception 
    {
        if (name == null || name.isEmpty())
            throw new ParseException("Colorspace must have a name");
        ColorSpace sp;
        switch(dimension)
        {
            case 0: throw new ParseException("Colorspace must have dimension 1 or greater: " + name);
            case 1:  
                sp = new LinearColorSpace(name, idcounter++);
                transformPoints = new ArrayList<RGBA>();
                break;
            case 2:
                sp = new FieldStrength2DColorSpace(name, idcounter++);
                transformPoints = new ArrayList<RGBA>();
                break;
            default: 
                throw new ParseException("Unsupported dimension for colorspace: " + dimension);
        }
        name = null;
        
        for (RGBA rgba : points)
        {
            switch(dimension)
            {
                case 1: 
                    transformPoints.add(rgba); break;
                case 2: 
                    transformPoints.add(rgba); break;
            }
        }
        switch(dimension)
        {
            case 1: ((LinearColorSpace)sp).setPoints(transformPoints); break;
            case 2: ((FieldStrength2DColorSpace)sp).setPoints(transformPoints); break;
            
            
        }
        sp.setStrength(strength == null ? Value.ONE : strength);
        return sp;
    }

    private static int idcounter = 0; 

    private int id;
    private ArrayList<RGBA> transformPoints;



                                

    
    
}
