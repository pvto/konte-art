package org.konte.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BackupFile {

    int storedCount = 0;
    int retrievedCount = 0;
    int objectSizeInBytes = 4;
    File filefile;
    OutputStream out;
    InputStream in;

    public BackupFile(File f) throws FileNotFoundException
    {
        filefile = f;
        out = new FileOutputStream(filefile);
    }

    public void prepareForRead() throws IOException
    {
        
        out.close();
        in = new FileInputStream(filefile);
    }

    public void read(byte[] bytes) throws IOException
    {
        int n = 0;
        while(n < bytes.length)
            n += in.read(bytes, n, bytes.length - n);
    }

}
