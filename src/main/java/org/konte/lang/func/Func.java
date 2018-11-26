
package org.konte.lang.func;

import org.konte.lang.Tokens;
import org.konte.lang.Tokens.ContextualOneToOneFunction;
import org.konte.lang.Tokens.Function3;
import org.konte.model.Model;

public class Func {

   public static class ESleep extends Tokens.Function {

        @Override public int getArgsCount() { return 1; }
        public ESleep(String name) { super(name); }

        @Override
        public float value(float... val)
        {
            long start = System.nanoTime();
            int millis = (int)val[0];
            try {
                Thread.sleep(millis / 1000, millis % 1000);
            } catch(Exception ex) {}
            long dur = System.nanoTime() - start;
            return (float)dur;
        }
    }

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
            return (float) Prob.irnd(model.getRandomFeed(), (int)val[0]);
        }
    }

    public static class ERndfbin extends ContextualOneToOneFunction
    {
        @Override public int getArgsCount() { return 2; }
        @Override public boolean nArgsAllowed(int n) { return n==2; }

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
        @Override public boolean nArgsAllowed(int n) { return n==2; }

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
        @Override public boolean nArgsAllowed(int n) { return n==3; }

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


    public static class EChoice extends Function3 {

        @Override public int getArgsCount() { return 3; }
        @Override public boolean nArgsAllowed(int n) { return n==3; }

        public EChoice(String name) { super(name); }

        @Override
        public float value(float... args) throws Exception {
            if (args[0] <= 0f) { return args[2]; }
            return args[1];
        }
    }

    public static class ERndfnme extends ContextualOneToOneFunction
    {
        @Override public int getArgsCount() { return 3; }
        @Override public boolean nArgsAllowed(int n) { return n==3; }

        public ERndfnme(String name, Model model) { super(name, model); }

        @Override
        public float value(float... val) throws Exception
        {
            if (!model.isPreEvaluated)
                throw new java.util.MissingResourceException("blocking preliminary access", this.getClass().getName(), "");
            double n = (double)val[0];
            double m = (double)val[1];
            double exp = (double)val[2];
            return (float) Prob.rndnme(n, m, exp, model.getRandomFeed());
        }
    }
}
