package org.konte.lang.func;

import java.util.List;
import org.konte.image.OutputShape;
import org.konte.lang.Tokens;
import org.konte.model.DrawingContext;
import org.konte.model.Model;

/** Neighborhood related functions, runtime model.
 *
 * @author Paavo Toivanen https://github.com/pvto
 */
public class Nb {

    public static class EContextSearchXyz extends Tokens.ContextualOneToOneFunction
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

    public static class EContextNearbyDistXyz extends Tokens.ContextualOneToOneFunction
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
            List<OutputShape> list = model.shapeReader.getRuleWriter().findAll(x, y, z, radius);
            for(OutputShape o : list)
            {
                double dist = Math.sqrt(
                        pow2(x - o.matrix.m03)
                        + pow2(y - o.matrix.m13)
                        + pow2(z - o.matrix.m23)
                ) - o.getAvgWidth() / 2.0;
                if (dist < minDist)
                {
                    minDist = dist;
                }
            }
            return (float)Math.max(0.0, minDist);
        }
    }
    
    private static double pow2(double x) { return x * x; }
    
    public static class EContextNbDist extends Tokens.ContextualTwoToOneFunction
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
            return (float)Math.sqrt(x0*x0 + y0*y0 + z0*z0) - p.getAvgWidth() / 2.0f;
        }
    }
    

    public static class EContextNbEval extends Tokens.ContextualTwoToOneFunction
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

}
