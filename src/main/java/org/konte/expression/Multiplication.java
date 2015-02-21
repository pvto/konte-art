package org.konte.expression;

import org.konte.lang.Language;
import org.konte.parse.ParseException;


public class Multiplication extends Operator {

    public Multiplication(Expression leading, Expression trailing) {
        super(leading, trailing);
        this.operator = Language.multiply;
    }

    @Override
    public Float evaluate() throws ParseException {
        return leading.evaluate() * trailing.evaluate();

    }

    public String toString() {
        return "(" + leading + "*" + trailing + ")";
    }
}
