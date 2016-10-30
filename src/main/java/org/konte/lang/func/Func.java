
package org.konte.lang.func;

import java.util.List;
import org.konte.image.OutputShape;
import org.konte.lang.Tokens.ContextualOneToOneFunction;
import org.konte.lang.Tokens.ContextualTwoToOneFunction;
import org.konte.model.DataTable;
import org.konte.model.DrawingContext;
import org.konte.model.Model;

public class Func {

    public static class EInc extends ContextualOneToOneFunction
    {
        @Override public int getArgsCount() { return 0; }
        private int i = 0;

        @Override
        public boolean nArgsAllowed(int n)
        {
            return n==0 || n==1;
        }

        public EInc(String name, Model model) { super(name, model); }
        
        @Override
        public float value(float... val) throws Exception
        {
            if (!model.isPreEvaluated)
                throw new java.util.MissingResourceException("blocking preliminary access", this.getClass().getName(), "");
            return i++;
        }
    }

    public static class ERndf extends ContextualOneToOneFunction
    {
        @Override public int getArgsCount() { return 0; }

        @Override
        public boolean nArgsAllowed(int n)
        {
            return n==0;
        }

        public ERndf(String name, Model model) { super(name, model); }
        
        @Override
        public float value(float... val) throws Exception
        {
            if (!model.isPreEvaluated)
                throw new java.util.MissingResourceException("blocking preliminary access", this.getClass().getName(), "");
            return (float)model.getRandomFeed().get();
        }
    }

    public static class EIrndf extends ContextualOneToOneFunction
    {
        @Override public int getArgsCount() { return 0; }

        @Override
        public boolean nArgsAllowed(int n)
        {
            return n==1;
        }

        public EIrndf(String name, Model model) { super(name, model); }
        
        @Override
        public float value(float... val) throws Exception
        {
            if (!model.isPreEvaluated)
                throw new java.util.MissingResourceException("blocking preliminary access", this.getClass().getName(), "");
            return (float) Math.floor(model.getRandomFeed().get() * val[0]);
        }
    }
    
    public static class ERndfbin extends ContextualOneToOneFunction
    {
        @Override public int getArgsCount() { return 2; }

        @Override
        public boolean nArgsAllowed(int n)
        {
            return n==2;
        }

        public ERndfbin(String name, Model model) { super(name, model); }
        
        @Override
        public float value(float... val) throws Exception
        {
            if (!model.isPreEvaluated)
                throw new java.util.MissingResourceException("blocking preliminary access", this.getClass().getName(), "");
            int n = (int)val[0];
            double p = (double)val[1];
            return (float) Prob.binmRnd(n, p, model.getRandomFeed().get());
        }
    }

    public static class ERndfhypg extends ContextualOneToOneFunction
    {
        @Override public int getArgsCount() { return 2; }

        @Override
        public boolean nArgsAllowed(int n)
        {
            return n==2;
        }

        public ERndfhypg(String name, Model model) { super(name, model); }
        
        @Override
        public float value(float... val) throws Exception
        {
            if (!model.isPreEvaluated)
                throw new java.util.MissingResourceException("blocking preliminary access", this.getClass().getName(), "");
            int N1 = (int)val[0];
            int N2 = (int)val[1];
            int n = (int)val[2];
            return (float) Prob.hypgRnd(N1, N2, n, model.getRandomFeed().get());
        }
    }
    
    public static class ERndfpareto extends ContextualOneToOneFunction
    {
        @Override public int getArgsCount() { return 3; }

        @Override
        public boolean nArgsAllowed(int n)
        {
            return n==3;
        }

        public ERndfpareto(String name, Model model) { super(name, model); }
        
        @Override
        public float value(float... val) throws Exception
        {
            if (!model.isPreEvaluated)
                throw new java.util.MissingResourceException("blocking preliminary access", this.getClass().getName(), "");
            double alpha = (double)val[0];
            double minx = (double)val[1];
            double extent = (double)val[2];

            return (float) Prob.paretoRnd(alpha, minx, extent, model.getRandomFeed());
        }
    }
    


}
