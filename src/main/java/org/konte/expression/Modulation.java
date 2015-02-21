
package org.konte.expression;


import org.konte.lang.Language;
import org.konte.parse.ParseException;


public class Modulation extends Operator {
        public Modulation(Expression leading, Expression trailing) {
            super(leading, trailing);
            this.operator = Language.modulo;
        }


        @Override
        public Float evaluate() throws ParseException {
            return leading.evaluate() % trailing.evaluate();

        }

        public String toString() {
            return "(" + leading + "%" + trailing + ")";
        }
}
