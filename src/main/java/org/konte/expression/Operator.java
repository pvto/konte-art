package org.konte.expression;

import org.konte.lang.Tokens.Token;
import org.konte.parse.ParseException;

public class Operator implements Expression {

    protected Expression leading;
    protected Expression trailing;
    Token operator;
    protected int resolved = 0;
    protected Float value = null;

    public Operator() {
    }

    public Operator(Expression leading, Expression trailing) {
        this.leading = leading;
        this.trailing = trailing;
    }

    public boolean findExpression(Class c) {
        if (c.isInstance(this)) {
            return true;
        }
        return (leading != null ? leading.findExpression(c) : false)
                || (trailing != null ? trailing.findExpression(c) : false);
    }

    public Float evaluate() throws ParseException {
        return null;
    }

    public Expression getLeading() {
        return leading;
    }

    public Expression getTrailing() {
        return trailing;
    }

    public void setLeading(Expression expression) {
        this.leading = expression;
    }

    public void setTrailing(Expression trailing) {
        this.trailing = trailing;
    }

    public String toString() {
        return leading.toString() + "(x)" + trailing.toString();
    }
}
