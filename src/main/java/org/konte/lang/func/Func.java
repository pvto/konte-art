
package org.konte.lang.func;

import org.konte.lang.Tokens.ContextualOneToOneFunction;
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
                throw new Exception("blocking preliminary access");
            return i++;
        }
    }
}
