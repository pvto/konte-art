
package org.konte.model;

import org.konte.expression.Expression;
import org.konte.parse.ParseException;

/**
 *
 * @author pvto
 */
public abstract class ColorSpaceImpl implements ColorSpace {

    protected int id;
    protected String name;
    protected Expression strength;

    public void setId(int id)
    {
        this.id = id;
    }
    public int getId()
    {
        return id;
    }

    public void setStrength(Expression strength)
    {
        this.strength = strength;
    }
    public float getStrength() throws ParseException 
    {
        return strength.evaluate();
    }  
    
    public String getName()
    {
        return name;
    }
    
    public abstract int getDimension();

    public abstract float[] getValue(float x) throws ParseException;

}
