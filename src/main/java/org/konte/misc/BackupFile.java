package org.konte.misc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

public class BackupFile {

    int storedCount = 0;
    int retrievedCount = 0;
    int objectSizeInBytes = 4;
    RandomAccessFile file;
    File filefile;

    public BackupFile(File f) throws FileNotFoundException
    {
        file = new RandomAccessFile(f, "rw");
        filefile = f;
    }
    
}
