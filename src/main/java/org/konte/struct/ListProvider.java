package org.konte.struct;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Paavo Toivanen https://github.com/pvto
 */
public interface ListProvider<T> {
    
    
    <T> List<T> getList(int expectedListSize, int treeCurrentSize);

    
    
    
    
    public static final ListProvider LP_LINKEDLIST = new ListProvider() {
        @Override
        public List getList(int expectedListSize, int treeCurrentSize)
        {
            return new LinkedList();
        }
    };
    
    public static final ListProvider LP_ARRAYLIST = new ListProvider() {
        @Override
        public List getList(int expectedListSize, int treeCurrentSize)
        {
            return new ArrayList(expectedListSize);
        }
    };
}

