/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.konte.model;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author pto
 */
public class MeshIndex {

    public HashMap<Integer,Mesh> index;
    private int meshId;
    private int rowId;
    private int wrapId;
    
    public MeshIndex(Model m) {
        index = new HashMap<Integer, Mesh>();
        meshId = m.getNameExpressionId("mesh");
        rowId = m.getNameExpressionId("row");
        wrapId = m.getNameExpressionId("wrap");
    }
    
    public void add(DrawingContext p) {
        int meshIndex = (int)p.getDef(meshId);
        Mesh m = index.get(meshIndex);
        if (m==null) {
            index.put(meshIndex, m=new Mesh((int)p.getDef(wrapId)));
        }
        m.addMeshElement((int)p.getDef(rowId), p);
    }
    
    public static enum Wrap { X(2),Y(1),BOTH(0),NO_WRAP(3);
        private Wrap(int i) { this.code = i; }
        private int code;
        public static Wrap getWrap(float i) { return getWrap((int)i); }
        private static Wrap getWrap(int i) {
            for(Wrap w : values())
                if (w.code==i)
                    return w;
            return BOTH;
        }
    }
    public static class Mesh {
        public ArrayList<ArrayList<DrawingContext>> rows;
        private int rowIndex;
        private ArrayList<DrawingContext> currentRow;
        public Wrap wrap;

        public Mesh(int wrapType) {
            rows = new ArrayList<ArrayList<DrawingContext>>();
            rowIndex = -1;
            wrap = Wrap.getWrap(wrapType);
        }
        public void addMeshElement(int row,DrawingContext p) {
            if (rowIndex < row) {
                rows.add(currentRow = new ArrayList<DrawingContext>());
                rowIndex++;
                row = rowIndex;
            } else
                currentRow = rows.get(row);
            int col = currentRow.size();
            currentRow.add(p);
            if (col<=0 || row ==0)
                return;
            ArrayList<DrawingContext> row2;
            row2 = rows.get(row-1);
            DrawingContext p1,p2,p3,p4;
            if (row2.size() < col+1) 
                return;
            p1 = row2.get(col-1);
            p2 = row2.get(col);
            p3 = currentRow.get(col-1);
            p4 = currentRow.get(col);
            p.shape = new MeshSqu(p1,p2,p4,p3);
            row2.set(col-1, null);  // dispose of - won't be used any more!

        }
    }
    
}
