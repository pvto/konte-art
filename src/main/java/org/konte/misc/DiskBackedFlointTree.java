package org.konte.misc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author pvto https://github.com/pvto
 */
public class DiskBackedFlointTree extends FlointTree {
    
    private final List<BackupFile> backupFiles = new ArrayList<>();
    private final Serializer serializer;


    public DiskBackedFlointTree(Serializer serializer)
    {
        this.serializer = serializer;
    }
    
    public class BagWrapper {
        private int ref = -1;
        public Object val;
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
    

    public void flushToDiskAssumeConstantSizeObjects() throws IOException
    {
        String prefix = "dbtmb" + "-" + System.currentTimeMillis() + Math.round(Math.random() * 10);
        File tmpFile = File.createTempFile(prefix, ".tmp");
        BackupFile backupFile = new BackupFile(tmpFile);
        
        //fix object size
        out: for(Node1 n1 : root.children) if (n1 != null)
            for(Node2 n2 : n1.children) if (n2 != null)
                for(Node3 n3 : n2.children) if (n3 != null)
                    for(Node4 n4 : n3.children) if (n4 != null)
                        for(Node5 n5 : n4.children) if (n5 != null)
                            for(Node6 n6 : n5.children) if (n6 != null)
                            {
                                FUPair fu = n6.firstChild;
                                while(fu != null)
                                {
                                    if (fu.u instanceof BagWrapper)
                                    {
                                        fu = fu.next;
                                        continue;
                                    }
                                    backupFile.objectSizeInBytes = serializer.objectSize(fu.u);
                                    break out;
                                }
                            }
        
        //transform what is left to wrapped entries
        for(Node1 n1 : root.children) if (n1 != null)
            for(Node2 n2 : n1.children) if (n2 != null)
                for(Node3 n3 : n2.children) if (n3 != null)
                    for(Node4 n4 : n3.children) if (n4 != null)
                        for(Node5 n5 : n4.children) if (n5 != null)
                            for(Node6 n6 : n5.children) if (n6 != null)
                            {
                                FUPair fu = n6.firstChild;
                                while(fu != null)
                                {
                                    if (!(fu.u instanceof BagWrapper))
                                    {
                                        BagWrapper w = new BagWrapper();
                                        w.val = fu.u;
                                        fu.u = w;
                                    }
                                    fu = fu.next;
                                }
                            }
        //write all entries that hold content to disk
        int 
                positionRef = 0,
                fileRef = backupFiles.size()
                ;
        for(Node1 n1 : root.children) if (n1 != null)
            for(Node2 n2 : n1.children) if (n2 != null)
                for(Node3 n3 : n2.children) if (n3 != null)
                    for(Node4 n4 : n3.children) if (n4 != null)
                        for(Node5 n5 : n4.children) if (n5 != null)
                            for(Node6 n6 : n5.children) if (n6 != null)
                            {
                                FUPair fu = n6.firstChild;
                                while(fu != null)
                                {
                                    BagWrapper w = (BagWrapper)fu.u;
                                    if (w.ref == -1)
                                    {
                                        byte[] bytes = serializer.marshal(w.val);
                                        backupFile.file.write(bytes);
                                        backupFile.storedCount++;

                                        w.ref = (positionRef++ << 10) | fileRef;
                                        w.val = null;
                                    }
                                    fu = fu.next;
                                }
                            }
        backupFiles.add(backupFile);
    }
    
    
    public void freeDiskCache() throws IOException
    {
        for(BackupFile backupFile : backupFiles)
        {
            backupFile.file.close();
            backupFile.filefile.delete();
        }
    }

}
