package org.konte.lang.func;

import org.konte.lang.Tokens;
import org.konte.model.DataTable;
import org.konte.model.Model;

/** Functions to deal with tabular data, runtime model.
 *
 * @author Paavo Toivanen https://github.com/pvto
 */
public class Table {

    
    
    public static class ECsv extends Tokens.ContextualOneToOneFunction
    {
        @Override public int getArgsCount() { return 3; }

        @Override
        public boolean nArgsAllowed(int n)
        {
            return n==3;
        }

        public ECsv(String name, Model model)
        { 
            super(name, model);
            if (model != null)
                model.enableContextSearch = true;
        }
        
        @Override
        public float value(float... val) throws Exception
        {
            if (!model.isPreEvaluated)
                throw new java.util.MissingResourceException("blocking preliminary access", this.getClass().getName(), "");
            int tind = (int)val[0],
                row = (int)val[1];
            int col = 1;
            if (val.length > 2)
                col = Math.max(1, (int)val[2]);

            DataTable table = model.dataTables.get(tind);
            if (table == null)
                return 0f;
            Object o = table.data.get(row-1)[col-1];
            if (o instanceof Float)
                return (Float)o;
            return 0f;
        }
    }
    
    public static class ETabColMin extends Tokens.ContextualOneToOneFunction
    {
        @Override public int getArgsCount() { return 2; }

        @Override
        public boolean nArgsAllowed(int n)
        {
            return n==2;
        }

        public ETabColMin(String name, Model model)
        { 
            super(name, model);
            if (model != null)
                model.enableContextSearch = true;
        }
        
        @Override
        public float value(float... val) throws Exception
        {
            if (!model.isPreEvaluated)
                throw new java.util.MissingResourceException("blocking preliminary access", this.getClass().getName(), "");
            int tind = (int)val[0],
                col = (int)val[1];

            DataTable table = model.dataTables.get(tind);
            if (table == null)
                return 0f;
            Float min = Float.MAX_VALUE;
            for(int i = 0; i < table.data.size(); i++)
            {
                Object o = table.data.get(i)[col-1];
                if (o instanceof Float)
                    min = Math.min((Float)o, min);
            }
            if (min == Float.MAX_VALUE)
            {
                return 0f;
            }
            return min;
        }
    }

    
    public static class ETabColMax extends Tokens.ContextualOneToOneFunction
    {
        @Override public int getArgsCount() { return 2; }

        @Override
        public boolean nArgsAllowed(int n)
        {
            return n==2;
        }

        public ETabColMax(String name, Model model)
        { 
            super(name, model);
            if (model != null)
                model.enableContextSearch = true;
        }
        
        @Override
        public float value(float... val) throws Exception
        {
            if (!model.isPreEvaluated)
                throw new java.util.MissingResourceException("blocking preliminary access", this.getClass().getName(), "");
            int tind = (int)val[0],
                col = (int)val[1];

            DataTable table = model.dataTables.get(tind);
            if (table == null)
                return 0f;
            Float max = Float.MIN_VALUE;
            for(int i = 0; i < table.data.size(); i++)
            {
                Object o = table.data.get(i)[col-1];
                if (o instanceof Float)
                    max = Math.max((Float)o, max);
            }
            if (max == Float.MIN_VALUE)
            {
                return 0f;
            }
            return max;
        }
    }

    public static class ETabColSum extends Tokens.ContextualOneToOneFunction
    {
        @Override public int getArgsCount() { return 2; }

        @Override
        public boolean nArgsAllowed(int n)
        {
            return n==2;
        }

        public ETabColSum(String name, Model model)
        { 
            super(name, model);
            if (model != null)
                model.enableContextSearch = true;
        }
        
        @Override
        public float value(float... val) throws Exception
        {
            if (!model.isPreEvaluated)
                throw new java.util.MissingResourceException("blocking preliminary access", this.getClass().getName(), "");
            int tind = (int)val[0],
                col = (int)val[1];

            DataTable table = model.dataTables.get(tind);
            if (table == null)
                return 0f;
            Float sum = 0f;
            for(int i = 0; i < table.data.size(); i++)
            {
                Object o = table.data.get(i)[col-1];
                if (o instanceof Float)
                    sum += (Float)o;
            }
            return sum;
        }
    }
    
    public static class ETabLength extends Tokens.ContextualOneToOneFunction
    {
        @Override public int getArgsCount() { return 1; }

        @Override
        public boolean nArgsAllowed(int n)
        {
            return n==1;
        }

        public ETabLength(String name, Model model)
        { 
            super(name, model);
            if (model != null)
                model.enableContextSearch = true;
        }
        
        @Override
        public float value(float... val) throws Exception
        {
            if (!model.isPreEvaluated)
                throw new java.util.MissingResourceException("blocking preliminary access", this.getClass().getName(), "");
            int tind = (int)val[0];

            DataTable table = model.dataTables.get(tind);
            if (table == null)
                return 0f;
            return table.data.size();
        }
    }

    public static class ETabAddRow extends Tokens.ContextualOneToOneFunction
    {
        @Override public int getArgsCount() { return -1; }

        @Override
        public boolean nArgsAllowed(int n)
        {
            return n > 1;
        }

        public ETabAddRow(String name, Model model)
        { 
            super(name, model);
            if (model != null)
                model.enableContextSearch = true;
        }
        
        @Override
        public float value(float... val) throws Exception
        {
            if (!model.isPreEvaluated)
                throw new java.util.MissingResourceException("blocking preliminary access", this.getClass().getName(), "");
            int tind = (int)val[0];

            DataTable table = model.dataTables.get(tind);
            if (table == null)
                return -1f;
            
            Object[] row = new Object[val.length - 1];
            for(int i = 1; i <= row.length; i++)
                row[i - 1] = val[i];
            table.addRow(row);
            return 1f;
        }
    }
    
    public static class ETabSetVal extends Tokens.ContextualOneToOneFunction
    {
        @Override public int getArgsCount() { return -1; }

        @Override
        public boolean nArgsAllowed(int n)
        {
            return n == 4;
        }

        public ETabSetVal(String name, Model model)
        { 
            super(name, model);
            if (model != null)
                model.enableContextSearch = true;
        }
        
        @Override
        public float value(float... val) throws Exception
        {
            if (!model.isPreEvaluated)
                throw new java.util.MissingResourceException("blocking preliminary access", this.getClass().getName(), "");
            int tind = (int)val[0],
                row = (int)val[1],
                col = (int)val[2];
            float toSet = val[3];
            
            DataTable table = model.dataTables.get(tind);
            if (table == null)
                return 0f;
            table.setVal(row-1, col-1, toSet);
            return toSet;
        }
    }
    
    public static class ETabIncVal extends Tokens.ContextualOneToOneFunction
    {
        @Override public int getArgsCount() { return -1; }

        @Override
        public boolean nArgsAllowed(int n)
        {
            return n == 4;
        }

        public ETabIncVal(String name, Model model)
        { 
            super(name, model);
            if (model != null)
                model.enableContextSearch = true;
        }
        
        @Override
        public float value(float... val) throws Exception
        {
            if (!model.isPreEvaluated)
                throw new java.util.MissingResourceException("blocking preliminary access", this.getClass().getName(), "");
            int tind = (int)val[0],
                row = (int)val[1],
                col = (int)val[2];
            float toAdd = val[3];
            
            DataTable table = model.dataTables.get(tind);
            if (table == null)
                return 0f;
            table.setVal(row-1, col-1, (Float)table.value(row-1, col-1) + toAdd);
            return toAdd;
        }
    }
    
    public static class ETabMulVal extends Tokens.ContextualOneToOneFunction
    {
        @Override public int getArgsCount() { return -1; }

        @Override
        public boolean nArgsAllowed(int n)
        {
            return n == 4;
        }

        public ETabMulVal(String name, Model model)
        { 
            super(name, model);
            if (model != null)
                model.enableContextSearch = true;
        }
        
        @Override
        public float value(float... val) throws Exception
        {
            if (!model.isPreEvaluated)
                throw new java.util.MissingResourceException("blocking preliminary access", this.getClass().getName(), "");
            int tind = (int)val[0],
                row = (int)val[1],
                col = (int)val[2];
            float multiplier = val[3];
            
            DataTable table = model.dataTables.get(tind);
            if (table == null)
                return 0f;
            table.setVal(row-1, col-1, (Float)table.value(row-1, col-1) * multiplier);
            return multiplier;
        }
    }
    
    public static class ETabClassVal extends Tokens.ContextualOneToOneFunction
    {
        @Override public int getArgsCount() { return -1; }

        @Override
        public boolean nArgsAllowed(int n)
        {
            return n == 3;
        }

        public ETabClassVal(String name, Model model)
        { 
            super(name, model);
            if (model != null)
                model.enableContextSearch = true;
        }
        
        @Override
        public float value(float... val) throws Exception
        {
            if (!model.isPreEvaluated)
                throw new java.util.MissingResourceException("blocking preliminary access", this.getClass().getName(), "");
            int tind = (int)val[0],
                row = (int)val[1],
                col = (int)val[2];
            
            DataTable table = model.dataTables.get(tind);
            if (table == null)
                return 0f;
            return (float) table.classValue(row, col);
        }
    }

}
