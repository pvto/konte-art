
package org.konte.expression;


import org.konte.lang.Language;
import org.konte.parse.ParseException;

    public class Power extends Operator {

        public Power(Expression leading, Expression trailing) {
            super(leading, trailing);
            this.operator = Language.pow_op;
        }


        @Override
        public Float evaluate() throws ParseException 
        {
            return (float)Math.pow(leading.evaluate(), trailing.evaluate());
        }

        public String toString() {
            return "(" + leading + "**" + trailing + ")";
        }
    }