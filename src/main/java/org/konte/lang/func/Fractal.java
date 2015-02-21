package org.konte.lang.func;

import org.apache.commons.math.complex.Complex;
import org.konte.lang.Tokens.Function;


public class Fractal {

    public static class EMandelbrot extends Function {

        @Override public int getArgsCount() { return 2; }
        public EMandelbrot(String name) { super(name); }
        
        @Override
        public float value(float... val)
        {
            Complex c = new Complex(val[0],val[1]); // c
            Complex z = c;
            int n = 1;
            while (z.abs() <= 2 && n < 256)
            {
                z = z.multiply(z).add(c);
                n++;
            }
            return (float)n;
        }
    }

    public static class EJulia extends Function {
        
        @Override public int getArgsCount() { return 4; }
        public EJulia(String name) { super(name); }
        
        @Override
        public float value(float... val)
        {
            Complex c = new Complex(val[0],val[1]); // c
            Complex z = new Complex(val[2],val[3]);
            int n = 1;
            while (z.abs() <= 2 && n < 256)
            {
                z = z.multiply(z).add(c);
                n++;
            }
            return (float)n;
        }
    }   
}
