
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
    
    public static class EContextSearchXyz extends ContextualOneToOneFunction
    {
        @Override public int getArgsCount() { return 4; }

        @Override
        public boolean nArgsAllowed(int n)
        {
            return n==4;
        }

        public EContextSearchXyz(String name, Model model)
        { 
            super(name, model);
            if (model != null)
                model.enableContextSearch = true;
        }
        
        @Override
        public float value(float... val) throws Exception
        {
            if (!model.isPreEvaluated)
                throw new java.util.MissingResourceException("blocking preliminary access", this.getClass().getName(), "");
            double x = (double)val[0],
                    y = (double)val[1],
                    z = (double)val[2],
                    radius = (double)val[3]
                    ;
            List<OutputShape> list = model.shapeReader.getRuleWriter().findAll(x, y, z, radius);
            return (float) list.size();
        }
    }

    public static class EContextNearbyDistXyz extends ContextualOneToOneFunction
    {
        @Override public int getArgsCount() { return 4; }

        @Override
        public boolean nArgsAllowed(int n)
        {
            return n==4;
        }

        public EContextNearbyDistXyz(String name, Model model)
        { 
            super(name, model);
            if (model != null)
                model.enableContextSearch = true;
        }
        
        @Override
        public float value(float... val) throws Exception
        {
            if (!model.isPreEvaluated)
                throw new java.util.MissingResourceException("blocking preliminary access", this.getClass().getName(), "");
            double x = (double)val[0],
                    y = (double)val[1],
                    z = (double)val[2],
                    radius = (double)val[3]
                    ;
            double minDist = Double.MAX_VALUE;
            OutputShape ret = null;
            List<OutputShape> list = model.shapeReader.getRuleWriter().findAll(x, y, z, radius);
            for(OutputShape o : list)
            {
                double dist = Math.sqrt(
                        pow2(x - o.matrix.m03)
                        + pow2(y - o.matrix.m13)
                        + pow2(z - o.matrix.m23)
                );
                double r = o.getAvgWidth() / 2.0;
                if (r >= dist)
                {
                    dist = 0.0;
                }
                else
                {
                    dist = dist - r;
                }
                if (dist < minDist)
                {
                    minDist = dist;
                    ret = o;
                }
            }
            return (float)minDist;
        }
    }
    
    private static double pow2(double x) { return x * x; }
    
    public static class EContextNbDist extends ContextualTwoToOneFunction
    {
        @Override public int getArgsCount() { return 6; }

        @Override
        public boolean nArgsAllowed(int n)
        {
            return n==4 || n==5 || n==6;
        }

        public EContextNbDist(String name, Model model)
        { 
            super(name, model);
            if (model != null)
                model.enableContextSearch = true;
        }
        
        @Override
        public float value(float... val) throws Exception
        {
            if (!model.isPreEvaluated)
                throw new java.util.MissingResourceException("blocking preliminary access", this.getClass().getName(), "");
            double x = (double)val[0],
                    y = (double)val[1],
                    z = (double)val[2],
                    radius = (double)val[3]
                    ;
            int n = val.length>4? Math.max(1, (int)val[4]) : 1;
            OutputShape p = model.shapeReader.getRuleWriter().findNthNearestNeighbor(x, y, z, radius, n, arg2);
            if (p == null)
            {
                return Float.MAX_VALUE;
            }
            double x0 = (x - p.matrix.m03),
                    y0 = (y - p.matrix.m13),
                    z0 = (z - p.matrix.m23)
                    ;
            return (float)Math.sqrt(x0*x0 + y0*y0 + z0*z0) * 2f - p.getAvgWidth();
        }
    }
    

    public static class EContextNbEval extends ContextualTwoToOneFunction
    {
        @Override public int getArgsCount() { return 6; }

        @Override
        public boolean nArgsAllowed(int n)
        {
            return n==4 || n==5 || n==6;
        }

        public EContextNbEval(String name, Model model)
        { 
            super(name, model);
            if (model != null)
                model.enableContextSearch = true;
        }
        
        @Override
        public float value(float... val) throws Exception
        {
            if (!model.isPreEvaluated)
                throw new java.util.MissingResourceException("blocking preliminary access", this.getClass().getName(), "");
            double x = (double)val[0],
                    y = (double)val[1],
                    z = (double)val[2],
                    radius = (double)val[3]
                    ;
            int n = val.length>4? Math.max(1, (int)val[4]) : 1;
            DrawingContext s = model.shapeReader.getRuleWriter().findNthNearestNeighborCtx(x, y, z, radius, n, null);
            if (s == null)
            {
                return 0f;
            }
            DrawingContext stacked = model.context;
            model.context = s;
            float ret = arg2.evaluate();
            model.context = stacked;
            return ret;
        }
    }

    
    
    public static class ECsv extends ContextualOneToOneFunction
    {
        @Override public int getArgsCount() { return 3; }

        @Override
        public boolean nArgsAllowed(int n)
        {
            return n==3;
        }

        public ECsv(String name, Model model)
        { 
            super(name, model);
            if (model != null)
                model.enableContextSearch = true;
        }
        
        @Override
        public float value(float... val) throws Exception
        {
            if (!model.isPreEvaluated)
                throw new java.util.MissingResourceException("blocking preliminary access", this.getClass().getName(), "");
            int tind = (int)val[0],
                row = (int)val[1],
                col = (int)val[2];

            DataTable table = model.dataTables.get(tind);
            if (table == null)
                return 0f;
            Object o = table.data.get(row-1)[col-1];
            if (o instanceof Float)
                return (Float)o;
            return 0f;
        }
    }
    
    public static class ETabColMin extends ContextualOneToOneFunction
    {
        @Override public int getArgsCount() { return 2; }

        @Override
        public boolean nArgsAllowed(int n)
        {
            return n==2;
        }

        public ETabColMin(String name, Model model)
        { 
            super(name, model);
            if (model != null)
                model.enableContextSearch = true;
        }
        
        @Override
        public float value(float... val) throws Exception
        {
            if (!model.isPreEvaluated)
                throw new java.util.MissingResourceException("blocking preliminary access", this.getClass().getName(), "");
            int tind = (int)val[0],
                col = (int)val[1];

            DataTable table = model.dataTables.get(tind);
            if (table == null)
                return 0f;
            Float min = Float.MAX_VALUE;
            for(int i = 0; i < table.data.size(); i++)
            {
                Object o = table.data.get(i)[col-1];
                if (o instanceof Float)
                    min = Math.min((Float)o, min);
            }
            if (min == Float.MAX_VALUE)
            {
                return 0f;
            }
            return min;
        }
    }

    
    public static class ETabColMax extends ContextualOneToOneFunction
    {
        @Override public int getArgsCount() { return 2; }

        @Override
        public boolean nArgsAllowed(int n)
        {
            return n==2;
        }

        public ETabColMax(String name, Model model)
        { 
            super(name, model);
            if (model != null)
                model.enableContextSearch = true;
        }
        
        @Override
        public float value(float... val) throws Exception
        {
            if (!model.isPreEvaluated)
                throw new java.util.MissingResourceException("blocking preliminary access", this.getClass().getName(), "");
            int tind = (int)val[0],
                col = (int)val[1];

            DataTable table = model.dataTables.get(tind);
            if (table == null)
                return 0f;
            Float max = Float.MIN_VALUE;
            for(int i = 0; i < table.data.size(); i++)
            {
                Object o = table.data.get(i)[col-1];
                if (o instanceof Float)
                    max = Math.max((Float)o, max);
            }
            if (max == Float.MIN_VALUE)
            {
                return 0f;
            }
            return max;
        }
    }

    public static class ETabLength extends ContextualOneToOneFunction
    {
        @Override public int getArgsCount() { return 1; }

        @Override
        public boolean nArgsAllowed(int n)
        {
            return n==1;
        }

        public ETabLength(String name, Model model)
        { 
            super(name, model);
            if (model != null)
                model.enableContextSearch = true;
        }
        
        @Override
        public float value(float... val) throws Exception
        {
            if (!model.isPreEvaluated)
                throw new java.util.MissingResourceException("blocking preliminary access", this.getClass().getName(), "");
            int tind = (int)val[0];

            DataTable table = model.dataTables.get(tind);
            if (table == null)
                return 0f;
            return table.data.size();
        }
    }

}
