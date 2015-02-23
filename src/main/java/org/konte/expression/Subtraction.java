package org.konte.expression;

import org.konte.lang.Language;
import org.konte.parse.ParseException;

public class Subtraction extends Operator {

    public Subtraction() {
    }

    public Subtraction(Expression leading, Expression trailing) {
        super(leading, trailing);
        this.operator = Language.subtract;
    }

    @Override
    public Float evaluate() throws ParseException 
    {
        return leading.evaluate() - trailing.evaluate();
    }

    @Override
    public String toString() {
        return "(" + leading + "-" + trailing + ")";
    }
}
