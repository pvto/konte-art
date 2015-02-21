package org.konte.misc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * @author pvto https://github.com/pvto
 */
public class DiskBackedTreeMapBag extends TreeMap {
    
    private List<BackupFile> backupFiles = new ArrayList<BackupFile>();
    private final Serializer serializer;
    
    public static interface Serializer {
        byte[] marshal(Object o) throws IOException;
        Object unmarshal(byte[] bytes) throws IOException;
        int objectSize(Object o);
    }

    public DiskBackedTreeMapBag(Serializer serializer)
    {
        this.serializer = serializer;
    }
    
    public class BagWrapper {
        private int ref = -1;
        private Object val;
        public BagWrapper next;
        public BagWrapper last;
        public Object getValue() throws IOException
        {
            if (ref != -1)
            {
                BackupFile backupFile = backupFiles.get(ref & 0x1FF);
                backupFile.file.seek((ref >>> 10) * (long)backupFile.objectSizeInBytes);
                byte[] bytes = new byte[backupFile.objectSizeInBytes];
                backupFile.retrievedCount++;
                backupFile.file.read(bytes);
                val = serializer.unmarshal(bytes);
                ref = -1;
            }
            return val;
        }
    }
    
    @Override
    public Object put(Object key, Object value)
    {
        Object ex = get(key);
        if (ex == null)
        {
            super.put(key, value);
        }
        else if (ex instanceof BagWrapper)
        {
            BagWrapper orig = (BagWrapper)ex;
            BagWrapper last = orig.last;
            last.next = orig.last = new BagWrapper();
            last.next.val = value;
        }
        else
        {
            BagWrapper w = new BagWrapper();
            w.val = ex;
            w.next = w.last = new BagWrapper();
            w.next.val = value;
            super.put(key, w);
        }
        return null;
    }

    public void flushToDiskAssumeConstantSizeObjects() throws IOException
    {
        String prefix = "dbtmb" + "-" + System.currentTimeMillis() + Math.round(Math.random() * 10);
        File tmpFile = File.createTempFile(prefix, ".tmp");
        BackupFile backupFile = new BackupFile(tmpFile);
        
        Entry e = firstEntry();
   out: while(e != null)
        {
            Object o = e.getValue();
            if (!(o instanceof BagWrapper))
            {
                backupFile.objectSizeInBytes = serializer.objectSize(o);
                break;
            }
            BagWrapper w = (BagWrapper)o;
            while(w != null)
            {
                if (w.ref == -1)
                {
                    backupFile.objectSizeInBytes = serializer.objectSize(w.val);
                    break out;
                }
                w = w.next;
            }
            e = higherEntry(e);
        }
        
        e = firstEntry();
        int positionRef = 0;
        while(e != null)
        {
            Object o = e.getValue();
            if (!(o instanceof BagWrapper))
            {
                BagWrapper w = new BagWrapper();
                w.val = o;
                put(e.getKey(), w);
                o = w;
            }
            BagWrapper w = (BagWrapper)o;
            while(w != null)
            {
                if (w.ref != -1)
                {
                    //already stored;
                }
                else
                {
                    byte[] bytes = serializer.marshal(w.val);
                    backupFile.file.write(bytes);
                    backupFile.storedCount++;
                    
                    w.ref = (positionRef++ << 10) | backupFiles.size();
                    w.val = null;
                }
                w = w.next;
            }
            e = higherEntry(e);
        }
        backupFiles.add(backupFile);
    }
    
    public static class BackupFile {
        int storedCount = 0;
        int retrievedCount = 0;
        int objectSizeInBytes = 4;
        RandomAccessFile file;
        public BackupFile(File f) throws FileNotFoundException {
            file = new RandomAccessFile(f, "rw");
        }
    }
}
