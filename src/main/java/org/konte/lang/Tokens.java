package org.konte.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.konte.expression.Expression;
import org.konte.expression.Value;
import org.konte.model.Model;
import org.konte.model.TransformModifier;
import org.konte.parse.ParseException;

public class Tokens {

    /**A word in the language.  May contain aliases in itself.
     *
     */
    public static class Token {

        public String name;
        public ArrayList<String> aliases = new ArrayList<String>();

        public Token() {
        }

        public Token(String name) {
            this.name = name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void addAlias(String s) {
            aliases.add(s);
        }

        public int compareTo(String tokenName) {
            if (name.equals(tokenName)) {
                return 0;
            }
            for (String s : aliases) {
                if (s.equals(tokenName)) {
                    return 0;
                }
            }
            return -1;
        }

        public String toString() {
            StringBuilder bd = new StringBuilder();
            bd.append(name);
            if (aliases.size() > 0) {
                bd.insert(0, "[");
                for (String s : aliases) {
                    bd.append("/").append(s);
                }
                bd.append("]");
            }
            return bd.toString();
        }

        public String toHelpString() {
            return toString();
        }
    }

    public static class ControlToken extends Token {

        public ControlToken(String name) {
            super(name);
            // add each character in token name to language's set of control characters
            Language.addControlCharacters(name);
        }

        public void addAlias(String s) {
            super.addAlias(s);
            Language.addControlCharacters(s);
        }
    }

    /** Expression operator, like + and - and %.
     *

     */
    public static class Operator extends ControlToken {

        public Operator(String name) {
            super(name);
        }
    }

    /**<p>This is a type of token used in boolean expressions; takes one or two
     * arguments and returns a boolean result.
     *

     */
    public static class Comparator extends ControlToken {

        public Comparator(String name) {
            super(name);
        }
    }

    /**<p>Defines a special range for the Tokenizer
     * or Parser.
     */
    public static class Context extends ControlToken {

        public Context(String name) {
            super(name);
        }
    }

    /**A token that is used to transform model structures.
     *
     */
    public static abstract class InnerToken extends Token {

        private Integer[] paramcnts;
        private Class referenceClass;

        /**
         *
         * @param s name of new inner token
         * @param allowedParameterCounts integer list of what are the possible numbers of parameters
         */
        public InnerToken(String s, Integer... allowedParameterCounts) {
            this(s, null, allowedParameterCounts);
        }

        /**
         *
         * @param s name of new inner token
         * @param referenceClass class that does the matching transform to a point in konte space (@link TransformExpression)
         * @param allowedParameterCounts integer list of what are the possible numbers of parameters
         */
        public InnerToken(String s, Class referenceClass, Integer... allowedParameterCounts) {
            super(s);
            this.referenceClass = referenceClass;
            if (allowedParameterCounts != null && allowedParameterCounts.length > 0) {
                Arrays.sort(allowedParameterCounts);
                paramcnts = allowedParameterCounts;
            } else {
                paramcnts = new Integer[]{1};
            }
        }

        public boolean nParamsAllowed(int count) {
            for (Integer i : paramcnts) {
                if (i == -1) {
                    return true;
                }
                if (i == count) {
                    return true;
                }
                if (i > count) {
                    return false;
                }
            }
            return false;
        }

        public boolean higherParamCountAllowed(int count) {
            for (int i = paramcnts.length - 1; i >= 0; i--) {
                if (paramcnts[i] == -1) {
                    return true;
                }
                if (paramcnts[i] > count) {
                    return true;
                }
                if (paramcnts[i] <= count) {
                    return false;
                }
            }
            return false;
        }

        public Class getReferenceClass() {
            return referenceClass;
        }

        public TransformModifier newInstance(Expression e, Token t) {
            return null;
        }

        public TransformModifier newInstance(List<Expression> e, Token t) {
            return null;
        }

    }

    /**A token that is used to transform model structures and can be backreferenced.
     *

     */
    public static class InnerExpressiveToken extends InnerToken {

        public InnerExpressiveToken(String s, Integer... allowedParameterCounts) {
            super(s, null, allowedParameterCounts);
        }
        public InnerExpressiveToken(String s, Class referenceClass, Integer... allowedParameterCounts) {
            super(s, referenceClass, allowedParameterCounts);
        }
    }

    /**An affine transform token like keywords x,y,z,scalex,skewy...
     *

     */
    public static class InnerAffineToken extends InnerToken implements AffineTransform {

        public InnerAffineToken(String s, Class referenceClass, Integer... allowedParameterCounts) {
            super(s, referenceClass, allowedParameterCounts);
        }
    }

    /**An affine transform token that can be backreferenced.
     *

     */
    public static class InnerAffineExpressiveToken extends InnerExpressiveToken implements AffineTransform {

        public InnerAffineExpressiveToken(String s, Class referenceClass, Integer... allowedParameterCounts) {
            super(s, referenceClass, allowedParameterCounts);
        }
    }

    /**A function that can be used in expressions.
     *

     */
    public static abstract class Function extends Token {

        public Function() {
        }

        public Function(String name) {
            this.name = name;
        }

        public abstract float value(float... args) throws Exception;

        public abstract int getArgsCount();

        public boolean nArgsAllowed(int n) {
            if (getArgsCount() == -1 || getArgsCount() == n) {
                return true;
            }
            return false;
        }
    }

    public static abstract class ContextualFunction extends Function {

        protected Model model;

        public ContextualFunction(String name, Model model) {
            super(name);
            this.model = model;
        }

        public void setModel(Model model) {
            this.model = model;
        }

        public Model getModel() {
            return model;
        }
    }

    public static abstract class ContextualOneToOneFunction extends ContextualFunction {

        public ContextualOneToOneFunction(String name, Model model) {
            super(name, model);
        }
    }

    public static abstract class Function0 extends Function {
        public Function0(String name) { super(name); }
        public int getArgsCount() { return 0; }
        @Override
        public abstract float value(float... args) throws Exception;
    }

    public static abstract class Function1 extends Function {
        public Function1(String name) { super(name); }
        public int getArgsCount() { return 1; }
        @Override
        public abstract float value(float... args) throws Exception;
    }

    public static abstract class Function2 extends Function {
        public Function2(String name) { super(name); }
        public int getArgsCount() { return 2; }
        @Override
        public abstract float value(float... args) throws Exception;
    }

    public static abstract class FunctionN_ extends Function {
        public FunctionN_(String name) { super(name); }
        public int getArgsCount() { return -1; }
        @Override
        public abstract float value(float... args) throws Exception;
    }
    
    public static class Constant extends Token {

        public Expression value;
        int id;
        public boolean isMacro;
        public float constVal;

        public Constant(String name, Expression value, boolean isDef) throws ParseException 
        {
//        try {
            this.name = name;
            this.value = value;
            preEval(isDef);
//        } catch (ParseException ex) {

//        }
        }

        public void setId(int id) {
            this.id = id;
        }

        public void setValue(Expression value) {
            this.value = value;
        }

        public void preEval(boolean isDef) throws ParseException 
        {
            if (value instanceof Value || isDef) {
                isMacro = false;
                constVal = value.evaluate();
            } else {
                isMacro = true;
            }
        }
    }
}
