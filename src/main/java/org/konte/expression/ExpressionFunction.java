package org.konte.expression;

import java.util.Arrays;
import org.konte.lang.Tokens.Function;
import org.konte.parse.ParseException;

public class ExpressionFunction extends Operator {

    private final Function f;
    Expression[] args;
    private float[] resolvedArgs;

    public ExpressionFunction(Function f) {
        this.f = f;
        this.operator = f;
        this.args = new Expression[Math.max(0, f.getArgsCount())];
        resolvedArgs = new float[Math.max(0, f.getArgsCount())];
    }

    public void setArg(int i, Expression e) {
        if (args.length < i + 1) {
            Expression[] argsNew = i == 0 ? new Expression[1] : Arrays.copyOf(args, i + 1);
            float[] resNew = i == 0 ? new float[1] : Arrays.copyOf(resolvedArgs, i + 1);
            args = argsNew;
            resolvedArgs = resNew;
        }
        args[i] = e;
    }

    public Expression[] getArgs() {
        return args;
    }

    public Float evaluate() throws ParseException 
    {
        for (int i = 0; i < resolvedArgs.length; i++) {
            resolvedArgs[i] = args[i].evaluate();
        }
        try {
            return f.value(resolvedArgs);
        }
        catch(java.util.MissingResourceException me)
        {
            return null;
        }
        catch (Exception e) 
        {
            e.printStackTrace();
            throw new ParseException("error evaluating " + this + ":" + e.getMessage());
        }
    }

    public int opCount() {
        return f.getArgsCount() < 0 ? Integer.MAX_VALUE : f.getArgsCount();
    }

    public Function getToken() {
        return f;
    }

    public String toString() {
        StringBuilder bd = new StringBuilder();
        bd.append(f.name);
        bd.append("(");
        for (Expression e : this.args) {
            bd.append(e).append(" ");
        }
        bd.append(")");

        return bd.toString();
    }
}
