package org.konte.lang.func;

import org.konte.lang.Tokens;
import org.konte.model.GreyBoxSystem;
import org.konte.model.Model;

/**
 *
 * @author Paavo Toivanen https://github.com/pvto
 */
public class GBSysFunc {

    public static class ESysEval extends Tokens.ContextualOneToOneFunction
    {
        @Override public int getArgsCount() { return -1; }

        @Override
        public boolean nArgsAllowed(int n)
        {
            return true;
        }

        public ESysEval(String name, Model model)
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

            int sysInd = (int)val[0];

            GreyBoxSystem sys = model.greyBoxSystems.get(sysInd);
            if (sys == null)
                return 0f;
            sys.evaluate(val);
            return 1f;
        }
    }
    
    public static class ESysRead extends Tokens.ContextualOneToOneFunction
    {
        @Override public int getArgsCount() { return -1; }

        @Override
        public boolean nArgsAllowed(int n)
        {
            return true;
        }

        public ESysRead(String name, Model model)
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

            int sysInd = (int)val[0];

            GreyBoxSystem sys = model.greyBoxSystems.get(sysInd);
            if (sys == null)
                return 0f;
            return sys.read(val);
        }
    }
    
    public static class ESysWrite extends Tokens.ContextualOneToOneFunction
    {
        @Override public int getArgsCount() { return -1; }

        @Override
        public boolean nArgsAllowed(int n)
        {
            return true;
        }

        public ESysWrite(String name, Model model)
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

            int sysInd = (int)val[0];

            GreyBoxSystem sys = model.greyBoxSystems.get(sysInd);
            if (sys == null)
                return 0f;
            sys.write(val);
            return 1f;
        }
    }
}
