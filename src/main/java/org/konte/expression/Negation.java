package org.konte.expression;

import org.konte.lang.Language;
import org.konte.parse.ParseException;

public class Negation extends Operator {

    /** for extension by BooleanExpressions */
    public Negation() {
    }

    public Negation(Expression trailing) {
        super(null, trailing);
        this.operator = Language.subtract;
    }

    @Override
    public Float evaluate() throws ParseException {
        return -trailing.evaluate();
    }

    @Override
    public String toString() {
        return "-(" + trailing + ")";
    }
}
