package org.konte.misc;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class Readers {

    public static StringBuilder load(InputStream in) throws IOException
    {
        StringBuilder b = new StringBuilder();
        byte[] bb = new byte[256*256];
        int size = 0;
        for (;;)
        {
            size = in.read(bb);
            for(int i=0;i<Math.min(10, size);i++)
            {
                int val = (int)(bb[i] & 0xFF);
                if (val < 0x0A)
                {
                    throw new IOException(String.format("Cannot open binary file, found %06x", val));
                }
            }
            if (size == -1)
            {
                break;
            }
            b.append(new String(Arrays.copyOfRange(bb,0, size)));
        }
        return b;


    }


    public static StringBuilder fillStringBuilder(File f) throws IOException
    {
        InputStream in = new BufferedInputStream(new FileInputStream(f));
        return load(in);
    }
}