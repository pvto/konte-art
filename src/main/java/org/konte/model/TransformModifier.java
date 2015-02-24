package org.konte.model;

import java.util.List;
import org.konte.parse.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import org.konte.lang.Tokens.Token;
import org.konte.misc.Matrix4;
import org.konte.expression.Expression;
import static org.konte.misc.Mathc3.*;

/**<p>A collection of inner classes that are all TransformExpressions.
 * <p>These classes represent shape transforms in the language, for example
 * x, y, rx, A, and d, that are passed inside a { } block to modify a
 * branch.
 *
 * @author pvto
 */
public abstract class TransformModifier {

    public String toString() { return this.getClass().getSimpleName(); }





    public ArrayList<Expression> exprs = new ArrayList<Expression>();
    public Float[] values = new Float[1];
    /* The number of parameters to this expression */
    public int n = 0;
    public Token token;
    public boolean resolved = false;
    public Matrix4 transform;

    public TransformModifier()
    {
    }

    public TransformModifier(Expression expr, Token token)
    {
        addExpression(expr);
        this.token = token;
    }
    public TransformModifier(List<Expression> exprs, Token token)
    {
        if (values.length < exprs.size())
            values = Arrays.copyOf(values, exprs.size());
        for (Expression e: exprs) 
            addExpression(e);
        this.token = token;
    }    
    public void addExpression(Expression expr)
    {
        this.exprs.add(expr);
        n++;
    }

    public Float[] evaluateAll() throws ParseException 
    {
        for (int i = 0; i < n ; i++)
            values[i] = exprs.get(i).evaluate();
        return values;
    }
    /* For other transforms.
     */ 
    public abstract void updateSTVal(DrawingContext st, Float delta);
        
    
    public abstract void updateSTVal(DrawingContext st, Float[] delta);
/*        
    public abstract void setSTVal(DrawingContext st, Float[] delta);
    
    public abstract void setSTVal(DrawingContext st, Float delta);*/
    
    /* For spatial transforms.
     */ 
    public Matrix4 getTransform(Float[] f) throws ParseException 
    {
        if (resolved) return transform;
        return createTransform(f);
    }    
    public Matrix4 createTransform(Float[] f)
    {
        return transform;
    }    
/*
    public Matrix4 setTransform(Float[] f)
    {
        return transform;
    }    */
    /* For non-spatial transforms.
     */ 
    public boolean isInTransform() { return false; }
    
    
    
    
    
    
    
    
    
    
    
    public static abstract class Spatial extends TransformModifier {
        public Spatial(Expression expr, Token token)
        {
            super(expr, token);
        }        
        public Spatial(List<Expression> exprs, Token token)
        {
            super(exprs,token);
        }          
        @Override
        public boolean isInTransform() { return true; }

        @Override
        public void updateSTVal(DrawingContext st, Float delta)
        {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void updateSTVal(DrawingContext st, Float[] delta)
        {
            throw new UnsupportedOperationException("Not supported.");
        }


/*        
        @Override
        public void setSTVal(DrawingContext st, Float delta)
        {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void setSTVal(DrawingContext st, Float[] delta)
        {
            throw new UnsupportedOperationException("Not supported.");
        }        */
    }
    
    
    
    
    
    public static class x extends Spatial {
        public x(Expression expr, Token token)
        {
            super(expr, token);
        }
        @Override
        public Matrix4 createTransform(Float[] f)
        {
            return Matrix4.translation(f[0], 0, 0);
        }

        
    }
    public static class y extends Spatial {
        public y(Expression expr, Token token)
        {
            super(expr, token);
        }
        @Override
        public Matrix4 createTransform(Float[] f)
        {
            return Matrix4.translation(0, f[0], 0);
        }

    }
    public static class z extends Spatial {
        public z(Expression expr, Token token)
        {
            super(expr, token);
        }
        @Override
        public Matrix4 createTransform(Float[] f)
        {
            return Matrix4.translation(0, 0, f[0]);
        }

        
    }
    public static final float toRad = 3.14159265f / 180f;
    
    public static class rx extends Spatial {
        public rx(Expression expr, Token token)
        {
            super(expr, token);
        }
        @Override
        public Matrix4 createTransform(Float[] f)
        {
            return Matrix4.rotateX(f[0] * toRad);
        }

        
    }
    public static class ry extends Spatial {
        public ry(Expression expr, Token token)
        {
            super(expr, token);
        }
        @Override
        public Matrix4 createTransform(Float[] f)
        {
            return Matrix4.rotateY(f[0] * toRad);
        }

        
    }
    public static class rz extends Spatial {
        public rz(Expression expr, Token token)
        {
            super(expr, token);
        }
        @Override
        public Matrix4 createTransform(Float[] f)
        {
            return Matrix4.rotateZ(-f[0] * toRad);
        }

        
    }
    public static class flipx extends Spatial {
        public flipx(Expression expr, Token t)
        {
            super(expr, t);
        }
        @Override
        public Matrix4 createTransform(Float[] f)
        {
            return Matrix4.flipX(f[0] * toRad);
        }

        
    }

    public static class flipy extends Spatial {
        public flipy(Expression expr, Token t)
        {
            super(expr, t);
        }
        @Override
        public Matrix4 createTransform(Float[] f)
        {
            return Matrix4.flipY(f[0] * toRad);
        }

        
    }
    public static class flipz extends Spatial {
        public flipz(Expression expr, Token t)
        {
            super(expr, t);
        }
        @Override
        public Matrix4 createTransform(Float[] f)
        {
            return Matrix4.flipZ(f[0] * toRad);
        }

        
    }    
  
    public static class sx extends Spatial {
        public sx(Expression expr, Token token)
        {
            super(expr, token);
        }
        @Override
        public Matrix4 createTransform(Float[] f)
        {
            return Matrix4.scale(f[0], 1f, 1f);
        }

        
    }
    public static class sy extends Spatial {
        public sy(Expression expr, Token token)
        {
            super(expr, token);
        }
        @Override
        public Matrix4 createTransform(Float[] f)
        {
            return Matrix4.scale(1f, f[0], 1f);
        }

        
    }
    public static class sz extends Spatial {
        public sz(Expression expr, Token token)
        {
            super(expr, token);
        }
        @Override
        public Matrix4 createTransform(Float[] f)
        {
            return Matrix4.scale(1f, 1f, f[0]);
        }

        
    }
    
    public static class s extends Spatial {
        public s(List<Expression> exprs, Token token)
        {
            super(exprs, token);
        }
        @Override
        public Matrix4 createTransform(Float[] f)
        {
            switch (f.length)
            {
                case 1: return Matrix4.scale(f[0]);
                case 2: return Matrix4.scale(f[0],f[1], 1f);
                case 3: return Matrix4.scale(f[0],f[1],f[2]);
                default:
            } 
            return null;
        }

        
    }
    public static class skew extends Spatial {
        public skew(List<Expression> exprs, Token token)
        {
            super(exprs, token);
        }
        @Override
        public Matrix4 createTransform(Float[] f)
        {
            return Matrix4.skew(f[0]* toRad,f[1]* toRad,f[2]* toRad);
        }

        
    }

    public static class skewx extends Spatial {
        public skewx(Expression expr, Token token)
        {
            super(expr, token);
        }
        @Override
        public Matrix4 createTransform(Float[] f)
        {
            return Matrix4.skew(f[0]* toRad,0f,0f);
        }

        
    }
    public static class skewy extends Spatial {
        public skewy(Expression expr, Token token)
        {
            super(expr, token);
        }
        @Override
        public Matrix4 createTransform(Float[] f)
        {
            return Matrix4.skew(0f,f[0]* toRad,0f);
        }

        
    }
    public static class skewz extends Spatial {
        public skewz(Expression expr, Token token)
        {
            super(expr, token);
        }
        @Override
        public Matrix4 createTransform(Float[] f)
        {
            return Matrix4.skew(0f,0f,f[0]* toRad);
        }

        
    }

   

    public static class R extends TransformModifier {
        public R(Expression expr, Token token)
        {
            super(expr, token);
        }
        @Override
        public void updateSTVal(DrawingContext st, Float delta)
        {
            st.R = bounds1(st.R + delta);
        }

        @Override
        public void updateSTVal(DrawingContext st, Float[] delta)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        
    }
    public static class G extends TransformModifier {
        public G(Expression expr, Token token)
        {
            super(expr, token);
        }
        @Override
        public void updateSTVal(DrawingContext st, Float delta)
        {
            st.G = bounds1(st.G + delta);
        }

        @Override
        public void updateSTVal(DrawingContext st, Float[] delta)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        
    }
    public static class B extends TransformModifier {
        public B(Expression expr, Token token)
        {
            super(expr, token);
        }
        @Override
        public void updateSTVal(DrawingContext st, Float delta)
        {
            st.B = bounds1(st.B + delta);
        }

        @Override
        public void updateSTVal(DrawingContext st, Float[] delta)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        
    }
    
    public static class H extends TransformModifier {
        public H(Expression expr, Token token)
        {
            super(expr, token);
        }
        @Override
        public void updateSTVal(DrawingContext st, Float delta)
        {
            st.changeHue(delta);
        }

        @Override
        public void updateSTVal(DrawingContext st, Float[] delta)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        
    }

    public static class Sat extends TransformModifier {
        public Sat(Expression expr, Token token)
        {
            super(expr, token);
        }
        @Override
        public void updateSTVal(DrawingContext st, Float delta)
        {
            st.changeSat(delta);
        }

        @Override
        public void updateSTVal(DrawingContext st, Float[] delta)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        
    }
 
    public static class L extends TransformModifier {
        public L(Expression expr, Token token)
        {
            super(expr, token);
        }
        @Override
        public void updateSTVal(DrawingContext st, Float delta)
        {
            st.changeLighness(delta);
        }

        @Override
        public void updateSTVal(DrawingContext st, Float[] delta)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        
    }

    public static class A extends TransformModifier {
        public A(Expression expr, Token token)
        {
            super(expr, token);
        }
        @Override
        public void updateSTVal(DrawingContext st, Float delta)
        {
            st.A = bounds1(st.A + delta);
        }

        @Override
        public void updateSTVal(DrawingContext st, Float[] delta)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        

    }    
    public static class shading extends TransformModifier {
        public shading(Expression expr, Token token)
        {
            super(expr, token);
        }
        @Override
        public void updateSTVal(DrawingContext st, Float delta)
        {
            st.shading = (short)Math.round(delta);
        }
        @Override
        public void updateSTVal(DrawingContext st, Float[] delta)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        

    }    
    public static class col0 extends TransformModifier {
        public col0(Expression expr, Token token)
        {
            super(expr, token);
        }
        @Override
        public void updateSTVal(DrawingContext st, Float delta)
        {
            st.col0 = delta;
        }
        @Override
        public void updateSTVal(DrawingContext st, Float[] delta)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        
    }
    public static class col1 extends TransformModifier {
        public col1(Expression expr, Token token)
        {
            super(expr, token);
        }
        @Override
        public void updateSTVal(DrawingContext st, Float delta)
        {
            st.col1 = delta;
        }
        @Override
        public void updateSTVal(DrawingContext st, Float[] delta)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        
    }
    public static class col2 extends TransformModifier {
        public col2(Expression expr, Token token)
        {
            super(expr, token);
        }
        @Override
        public void updateSTVal(DrawingContext st, Float delta)
        {
            st.col2 = delta;
        }
        @Override
        public void updateSTVal(DrawingContext st, Float[] delta)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        
    }    

    public static class RGB extends TransformModifier {
        public RGB(List<Expression> lexprs, Token t)
        {
            super(lexprs, t);
        }
        @Override
        public void updateSTVal(DrawingContext st, Float[] delta)
        {
            if (delta.length == 1)
            {
                st.R = bounds1(st.R + delta[0]);
                st.G = bounds1(st.G + delta[0]);
                st.B = bounds1(st.B + delta[0]);
            }
            else
            {
                st.R = bounds1(delta[0]);
                st.G = bounds1(delta[1]);
                st.B = bounds1(delta[2]);
            }
        }

        @Override
        public void updateSTVal(DrawingContext st, Float delta)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        
    }    
    public static class HSLA extends TransformModifier {
        public HSLA(List<Expression> lexprs, Token t)
        {
            super(lexprs, t);
        }
        public void updateSTVal(DrawingContext st, Float[] delta)
        {
            st.changeHue(delta[0]);
            st.changeSat(delta[1]);
            st.changeLighness(delta[2]);
            st.A = bounds1(st.A + delta[3]);
        }

        @Override
        public void updateSTVal(DrawingContext st, Float delta)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        
    }
    public static class d extends TransformModifier {
        public d(Expression expr, Token token)
        {
            super(expr, token);
        }
        @Override
        public void updateSTVal(DrawingContext st, Float delta)
        {
            st.d = Math.round(delta);
        }

        @Override
        public void updateSTVal(DrawingContext st, Float[] delta)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        
    }

    public static class layer extends TransformModifier {

        public layer(Expression lexpr, Token t)
        {
            super(lexpr, t);
        }
        @Override
        public void updateSTVal(DrawingContext st, Float delta)
        {
            st.layer = Math.round((st.layer + delta) * 1000f) / 1000f ;
        }

        @Override
        public void updateSTVal(DrawingContext st, Float[] delta)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    
    public static class fov extends TransformModifier {
        public fov(Expression expr, Token token)
        {
            super(expr, token);
        }
        @Override
        public void updateSTVal(DrawingContext st, Float delta)
        {
            st.fov = (short)Math.round(delta);
        }

        @Override
        public void updateSTVal(DrawingContext st, Float[] delta)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }    


}
