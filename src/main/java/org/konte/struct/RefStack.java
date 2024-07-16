package org.konte.struct;

import java.io.IOException;
import java.io.Serializable;

public class RefStack<T extends Serializable> implements Serializable {

    private RefStack<T> prev;
    private T val;

    public RefStack() {
        this.prev = null;
    }
    public RefStack(RefStack<T> prev, T val) {
        this.prev = prev;
        this.val = val;
    }

    public StackRetVal<T> pop() {
        return new StackRetVal<T>(val, prev);
    }

    public StackRetVal<T> peek() {
        return new StackRetVal<T>(val, this);
    }

    public RefStack<T> push(T val) {
        return new RefStack<T>(this, val);
    }

    public int depth() {
        int depth = 0;
        RefStack it = this;
        for(; it != null; it = it.prev, depth++);
        return depth;
    }

    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        int depth = 0;
        RefStack it = this;
        for(; it != null; it = it.prev, depth++);
        Object[] tmp = new Object[depth];
        it = this;
        for(int ind = tmp.length - 1; it != null; it = it.prev, ind = ind - 1)
            tmp[ind] = it.val;
        out.writeObject(tmp);
    }

    private void debugPrintStack(Object[] tmp, String id) {
        StringBuilder bd = new StringBuilder(id).append(" [").append(tmp.length).append("] ");
        for (Object o : tmp) { 
            bd.append((o instanceof StackRetVal) ? ((StackRetVal)o).val : "i"+o).append(",");
        }
        System.out.println(bd.toString());
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        Object[] tmp = (Object[]) in.readObject();
        RefStack<T> ret = null;
        for(int i = 0; i < tmp.length; i++)
            ret = new RefStack(ret, (T)tmp[i]);
        this.val = ret.val;
        this.prev = ret.prev;
    }

    public static class StackRetVal<T extends Serializable> {
        public final T val;
        public final RefStack<T> stackRef;

        public StackRetVal(T val, RefStack<T> stackRef) {
            this.val = val;
            this.stackRef = stackRef;
        }
    }
}
