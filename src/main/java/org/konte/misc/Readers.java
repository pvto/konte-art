package org.konte.misc;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class Readers {

    public static StringBuilder load(InputStream in) throws IOException
    {
        return load(in, null);
    }
        
    public static StringBuilder load(InputStream in, Charset charset) throws IOException
    {
        final StringBuilder b = new StringBuilder();
        InputStreamReader ir;
        if (charset == null) ir = new InputStreamReader(in);
        else ir = new InputStreamReader(in, charset);
        BufferedReader br = new BufferedReader(ir);
        br.lines().forEach((String li) -> {b.append(li).append(System.lineSeparator());});
        br.close();
        return b;
    }


    public static StringBuilder fillStringBuilder(File f) throws IOException
    {
        InputStream in = new BufferedInputStream(new FileInputStream(f));
        return load(in);
    }
}