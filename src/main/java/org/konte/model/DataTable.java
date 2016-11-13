package org.konte.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Paavo Toivanen https://github.com/pvto
 */
public class DataTable {

    public String name;
    
    public String[] headers;
    public Map<String,Integer> headerMap = new HashMap<>();
    public List<Object[]> data = new ArrayList<>();
    
    public void init()
    {
        if (headers == null)
        {
            int size = data.get(0).length;
            headers = new String[size];
            for (int i = 0; i < headers.length; i++)
            {
                headers[i] = i + "";
            }
        }
        for(int i = 0; i < headers.length; i++)
        {
            headerMap.put(headers[i], i);
        }
    }
    
    public Object value(int index, int column)
    {
        if (index >= data.size())
            return 0;
        return data.get(index)[column];
    }
    
    public Object value(int index, String column)
    {
        Integer colInd = headerMap.get(index);
        if (colInd == null)
            return 0;
        return value(index, colInd);
    }
    
    public void addRow(Object[] row)
    {
        data.add(row);
    }
    
    public void setVal(int row, int col, float toSet) {
        int y = row;
        if (y < 0 || y >= data.size())
            throw new RuntimeException("table " + name + " does not contain a row at index " + (row + 1));
        int x = col;
        Object[] oo = data.get(y);
        if (x < 0 || x >= oo.length)
            throw new RuntimeException("table " + name + " has too few columns, can't insert value '" + toSet + "' in index (row=" + (row+1) + ", col=" + (col+1) + ")");
        oo[x] = toSet;
    }


    public static DataTable parse(InputStream in) throws IOException
    {
        DataTable table = new DataTable();
        
        BufferedReader br = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
        String line = null;
        
        int i = 0;
        String separator = ",";
        while((line = br.readLine()) != null && i++ < 100000000)
        {
            if (line.trim().isEmpty()) {
                continue;
            }
            if (i == 1)
            {
                if (line.contains(";"))
                {
                    separator = ";";
                }
                else if (!line.contains(","))
                {
                    separator ="\\s+";
                }
                String[] headers = line.split(separator);
                boolean allHeaders = true;
                for(String s : headers)
                {
                    if (!s.matches("\".+\"|\\w[\\w\\s0-9\\-\\._]+")
                            || s.matches("[0-9]+(\\.[0-9]+)?"))
                        allHeaders = false;
                }
                if (allHeaders)
                {
                    for(int h = 0; h < headers.length; h++)
                    {
                        headers[h] = headers[h].replaceAll("^\"([^\"]+)\"$", "$1");
                    }
                    table.headers = headers;
                    continue;
                }
            }
            String[] vals = line.split(separator); 
            Object[] objVals = new Object[vals.length];
            for (int j = 0; j < vals.length; j++) {
                String val = vals[j].replaceAll("^\"([^\"]+)\"$", "$1").replace("([0-9]+),([0-9]+)", "$1.$2");
                try
                {
                    Float f = Float.parseFloat(val);
                    objVals[j] = f;
                }
                catch(Exception ex)
                {
                    objVals[j] = val;
                }
            }
            table.addRow(objVals);
        }
        table.init();
        br.close();
        return table;
    }
}
