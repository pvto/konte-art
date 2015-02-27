package org.konte.expression;

import org.konte.parse.ParseException;

public interface BooleanExpression {

    
    public boolean bevaluate() throws ParseException;

    
    
    public static class Compare extends Subtraction
            implements BooleanExpression {

        protected Float passval;

        protected Float getPassval() {
            return passval;
        }

        public boolean bevaluate() throws ParseException 
        {
            if ((passval = evaluate()) != null) {
                return true;
            }
            return false;
        }

        public String toString() {
            return "?(" + leading + " " + this.getClass().getSimpleName() + " " + trailing + ")";
        }
    }

    public static class And extends Compare {
        public boolean bevaluate() throws ParseException 
        {
            Expression leading = getLeading();
            Expression trailing = getTrailing();
            return (Compare.class.isAssignableFrom(leading.getClass())
                    ? ((Compare)leading).bevaluate() : leading.evaluate() > 0f)
                    &&
                   (Compare.class.isAssignableFrom(trailing.getClass()) 
                    ? ((Compare)trailing).bevaluate() : trailing.evaluate() > 0f);
        }
    }

    public static class Or extends Compare {
        public boolean bevaluate() throws ParseException 
        {
            Expression leading = getLeading();
            Expression trailing = getTrailing();
            return (Compare.class.isAssignableFrom(leading.getClass())
                    ? ((Compare)leading).bevaluate() : leading.evaluate() > 0f)
                    ||
                    (Compare.class.isAssignableFrom(trailing.getClass())
                    ? ((Compare)trailing).bevaluate() : trailing.evaluate() > 0f)
                    ;
        }
    }

    public static class Equals extends Compare {

        public boolean bevaluate() throws ParseException 
        {
            if (super.bevaluate()) {
                return getPassval().compareTo(ZERO) == 0;
            }
            return false;
        }

    }

    public static class Ne extends Compare {

        public Ne() {
            super();
        }

        public Ne(Expression l, Expression t) {
            super();
            this.leading = l;
            this.trailing = t;
        }

        public boolean bevaluate() throws ParseException 
        {
            if (super.bevaluate()) {
                return getPassval().compareTo(ZERO) != 0;
            }
            return false;
        }

    }
    static Float ZERO = new Float(0f);

    public static class Lt extends Compare {

        public boolean bevaluate() throws ParseException 
        {
            if (super.bevaluate()) {
                return getPassval().compareTo(ZERO) < 0;
            }
            return false;
        }

    }

    public static class Lte extends Compare {

        public boolean bevaluate() throws ParseException 
        {
            if (super.bevaluate()) {
                return getPassval().compareTo(ZERO) <= 0;
            }
            return false;
        }

    }

    public static class Gt extends Compare {

        public boolean bevaluate() throws ParseException 
        {
            if (super.bevaluate()) {
                return getPassval().compareTo(ZERO) > 0;
            }
            return false;
        }

    }

    public static class Gte extends Compare {

        public boolean bevaluate() throws ParseException 
        {
            if (super.bevaluate()) {
                return getPassval().compareTo(ZERO) >= 0;
            }
            return false;
        }

    }

    public static class Dummy extends Compare {

        boolean retVal;

        public Dummy(boolean retVal) {
            this.retVal = retVal;
        }

        public boolean bevaluate() {
            return retVal;
        }
    }

}
