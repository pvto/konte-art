
package org.konte.model;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import org.junit.Test;

/**
 *
 * @author paavo
 */
public class DataTableTest {


    @Test
    public void testParse() throws UnsupportedEncodingException, IOException
    {
        String table =
                "\"Foo o\";Bar;Baz-0\n"+
                "1;2;3\n"+
                "1.1;\"2.2\";\"3.3\""
                ;
        InputStream in = new ByteArrayInputStream(table.getBytes("UTF-8"));
        DataTable dt = DataTable.parse(in);
        assertEquals(3, dt.headers.length);
        assertEquals("Baz-0", dt.headers[2]);
        assertNotNull(dt.headerMap);
        assertEquals(2, dt.data.size());
        assertEquals(3, dt.data.get(0).length);
        assertEquals(3.3f, (Float)dt.data.get(1)[2], 1e-6f);
    }

}