package org.konte.misc;

import org.konte.struct.FlointTree;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author pvto https://github.com/pvto
 */
public class DiskBackedFlointTree extends FlointTree {
    
    public final List<BackupFile> backupFiles = new ArrayList<>();
    private BackupFile backupFile;
    int fileRef;
    public final Serializer serializer;


    public DiskBackedFlointTree(Serializer serializer)
    {
        this.serializer = serializer;
    }


    public class DiskWrapper {
        private int ref = -1;
        public Object val;
        public Object getValue() throws IOException
        {
            if (ref != -1)
            {
                BackupFile backupFile = backupFiles.get(ref);
                byte[] bytes = new byte[backupFile.objectSizeInBytes];
                backupFile.read(bytes);
                backupFile.retrievedCount++;
                val = serializer.unmarshal(bytes);
                ref = -1;
            }
            return val;
        }
    }

    private void diskWrap(FUPair fu)
    {
        while(fu != null)
        {
            if (!(fu.u instanceof DiskBackedFlointTree.DiskWrapper))
            {
                DiskBackedFlointTree.DiskWrapper w = new DiskBackedFlointTree.DiskWrapper();
                w.val = fu.u;
                fu.u = w;
            }
            fu = fu.next;
        }
    }
    
    BinBranch.Do diskWrapDo = new BinBranch.Do() {
        @Override
        public void now(BinBranch bb)
        {
            diskWrap(bb.first);
        }
    };


    private void diskWrite(FUPair fu, int fileRef, BackupFile backupFile) throws IOException
    {
        while(fu != null)
        {
            DiskWrapper w = (DiskWrapper)fu.u;
            if (w.ref == -1)
            {
                byte[] bytes = serializer.marshal(w.val);
                backupFile.out.write(bytes);
                backupFile.storedCount++;

                w.ref = fileRef;
                w.val = null;
            }
            fu = fu.next;
        }
    }

    BinBranch.Do diskWriteDo = new BinBranch.Do() {
        @Override
        public void now(BinBranch bb)
        {
            try {
                diskWrite(bb.first, fileRef, backupFile);
            }
            catch(IOException iox)
            {
                throw new RuntimeException(iox);
            }
        }
    };
    
    public void flushToDiskAssumeConstantSizeObjects() throws IOException
    {
        String prefix = "dbtmb" + "-" + System.currentTimeMillis() + Math.round(Math.random() * 10);
        File tmpFile = File.createTempFile(prefix, ".tmp");
        backupFile = new BackupFile(tmpFile);
        
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
                                    if (fu.u instanceof DiskWrapper)
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
                                if (n6.optimization != null)
                                {
                                    n6.optimization.traverse(diskWrapDo);
                                    continue;
                                }
                                diskWrap(n6.firstChild);
                            }
        //write all entries that hold content to disk, from last to first 
        //(furthermost items will be drawn first)
        fileRef = backupFiles.size();
        backupFiles.add(backupFile);
        
        for(int i = root.children.length - 1; i >= 0; i--) { Node1 n1 = root.children[i]; if (n1 != null)
            for(int j = n1.children.length - 1; j >= 0; j--) { Node2 n2 = n1.children[j]; if (n2 != null)
                for(int k = n2.children.length - 1; k >= 0; k--) { Node3 n3 = n2.children[k]; if (n3 != null)
                    for(int L = n3.children.length - 1; L >= 0; L--) { Node4 n4 = n3.children[L]; if (n4 != null)
                        for(int m = n4.children.length - 1; m >= 0; m--) { Node5 n5 = n4.children[m]; if (n5 != null)
                            for(int n = n5.children.length - 1; n >= 0; n--) { Node6 n6 = n5.children[n]; if (n6 != null)
                                {
                                    if (n6.optimization != null)
                                    {
                                        n6.optimization.traverse(diskWriteDo);
                                        continue;
                                    }
                                    diskWrite(n6.firstChild, fileRef, backupFile);
                                }
                            }
                        }
                    }
                }
            }
        }
        backupFile.prepareForRead();
    }
    
    
    public void freeDiskCache() throws IOException
    {
        for(BackupFile backupFile : backupFiles)
        {
            backupFile.in.close();
            backupFile.filefile.delete();
        }
    }
    
    public interface Do {
        public void Do(Object o);
    }
    
    
    public void iterate(final Do doTo) throws IOException
    {
        final Do newDo = new Do() {
           @Override
            public void Do(Object o)
            {
                FlointTree.FUPair fu = (FlointTree.FUPair)o;
                try
                {
                    while(fu != null)
                    {
                        if (fu.u instanceof DiskBackedFlointTree.DiskWrapper)
                        {
                            DiskBackedFlointTree.DiskWrapper w = (DiskBackedFlointTree.DiskWrapper)fu.u;
                            doTo.Do(w.getValue());
                        }
                        else
                        {
                            doTo.Do(fu.u);
                        }
                        fu = fu.next;
                    }
                }
                catch(IOException iox)
                { 
                    throw new RuntimeException(iox);
                }
            }
        };
        
        BinBranch.Do bindo = new BinBranch.Do() {
            @Override
            public void now(BinBranch bb)
            {
                newDo.Do(bb.first);
            }
        };
        out: for(int i = root.children.length - 1; i >= 0; i--) { Node1 n1 = root.children[i]; if (n1 != null)
            for(int j = n1.children.length - 1; j >= 0; j--) { Node2 n2 = n1.children[j]; if (n2 != null)
                for(int k = n2.children.length - 1; k >= 0; k--) { Node3 n3 = n2.children[k]; if (n3 != null)
                    for(int L = n3.children.length - 1; L >= 0; L--) { Node4 n4 = n3.children[L]; if (n4 != null)
                        for(int m = n4.children.length - 1; m >= 0; m--) { Node5 n5 = n4.children[m]; if (n5 != null)
                            for(int n = n5.children.length - 1; n >= 0; n--) { Node6 n6 = n5.children[n]; if (n6 != null)
                                {
                                    if (n6.optimization != null)
                                    {
                                        n6.optimization.traverse(bindo);
                                        continue;
                                    }
                                    FlointTree.FUPair fu = n6.firstChild;
                                    newDo.Do(fu);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
