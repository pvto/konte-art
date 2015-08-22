package org.konte.misc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * @author pvto https://github.com/pvto
 */
public class DiskBackedTreeMapBag extends TreeMap {
    
    private final List<RABackupFile> backupFiles = new ArrayList<>();
    private final Serializer serializer;


    public DiskBackedTreeMapBag(Serializer serializer)
    {
        this.serializer = serializer;
    }
    
    public class BagWrapper {
        private int ref = -1;
        public Object val;
        public BagWrapper next;
        public BagWrapper last;
        public Object getValue() throws IOException
        {
            if (ref != -1)
            {
                RABackupFile backupFile = backupFiles.get(ref & 0x1FF);
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
        RABackupFile backupFile = new RABackupFile(tmpFile);
        
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
            e = higherEntry(e.getKey());
        }
        
        //transform what is left to wrapped entries
        e = firstEntry();
        int positionRef = 0;
        while(e != null)
        {
            Object o = e.getValue();
            if (!(o instanceof BagWrapper))
            {
                BagWrapper w = new BagWrapper();
                w.val = o;
                w.next = null;
                w.last = w;
                super.put(e.getKey(), w);
            } 
            e = higherEntry(e.getKey());
        }
        //write all entries that hold content to disk
        e = lastEntry();
        while(e != null)
        {

            BagWrapper w = (BagWrapper)e.getValue();
            while(w != null)
            {
                if (w.ref == -1)
                {
                    byte[] bytes = serializer.marshal(w.val);
                    backupFile.file.write(bytes);
                    backupFile.storedCount++;
                    
                    w.ref = (positionRef++ << 10) | backupFiles.size();
                    w.val = null;
                }
                w = w.next;
            }
            e = lowerEntry(e.getKey());
        }
        backupFiles.add(backupFile);
    }
    
    
    public void freeDiskCache() throws IOException
    {
        for(RABackupFile backupFile : backupFiles)
        {
            backupFile.file.close();
            backupFile.filefile.delete();
        }
    }

}
