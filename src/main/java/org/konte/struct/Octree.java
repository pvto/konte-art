package org.konte.struct;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.konte.expression.Expression;
import org.konte.image.OutputShape;
import org.konte.model.Model;

/**
 *
 * @author Paavo Toivanen https://github.com/pvto
 */
public class Octree<T> {

    public int LEAF_MAX_OBJECTS = 10;
    public boolean DYNAMIC_MAX_OBJECTS = false;
    public double MAX_OBJ_TARGET_EXPONENT = 0.5;
    private int size = 0;
    private int dirty = 0;
    public ListProvider<CoordHolder> LIST_PROVIDER = ListProvider.LP_LINKEDLIST;

    public class CoordHolder {
        public double x,y, z;
        public T o;
        public Oct oct;
        public CoordHolder(double x, double y, double z, T o, Oct oct) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.o = o;
            this.oct = oct;
        }
        public void replace()
        {
            oct.replace(this, 0);
        }
        public void remove()
        {
            if (oct.items.remove(this))
                size--;
        }
        public int depth()
        {
            int i = 0;
            Oct q = this.oct;
            while(q != null)
            {
                i++;
                q = q.parent;
            }
            return i;
        }
    }


    public class Oct {
        public Oct
                parent = null,
                ULN = null, // upper left corner near child ...
                ULF = null, // u,l, far
                URN = null,
                URF = null,
                LLN = null,
                LLF = null,
                LRN = null,
                LRF = null
                ;
        public double
                x1,y1,z1,
                x2,y2,z2
                ;
        public List<CoordHolder> items = LIST_PROVIDER.getList(LEAF_MAX_OBJECTS, size);


        public Oct(Oct parent, double x1, double y1, double z1, double x2, double y2, double z2)
        {
            this.parent = parent;
            this.x1 = x1;  this.y1 = y1;  this.z1 = z1;
            this.x2 = x2;  this.y2 = y2;  this.z2 = z2;
        }

        public void findAll(double X1, double Y1, double Z1, double X2, double Y2, double Z2, List<CoordHolder> ret)
        {
            if (ULN == null)
            {
                for(CoordHolder h : items)
                    if (h.x >= X1 && h.y >= Y1 && h.z >= Z1
                            && h.x <= X2 && h.y <= Y2 && h.z <= Z2)
                        ret.add(h);
                return;
            }
            if (overlap(ULN, X1, Y1, Z1, X2, Y2, Z2)) ULN.findAll(X1, Y1, Z1, X2, Y2, Z2, ret);
            if (overlap(ULF, X1, Y1, Z1, X2, Y2, Z2)) ULF.findAll(X1, Y1, Z1, X2, Y2, Z2, ret);
            if (overlap(URN, X1, Y1, Z1, X2, Y2, Z2)) URN.findAll(X1, Y1, Z1, X2, Y2, Z2, ret);
            if (overlap(URF, X1, Y1, Z1, X2, Y2, Z2)) URF.findAll(X1, Y1, Z1, X2, Y2, Z2, ret);
            if (overlap(LLN, X1, Y1, Z1, X2, Y2, Z2)) LLN.findAll(X1, Y1, Z1, X2, Y2, Z2, ret);
            if (overlap(LLF, X1, Y1, Z1, X2, Y2, Z2)) LLF.findAll(X1, Y1, Z1, X2, Y2, Z2, ret);
            if (overlap(LRN, X1, Y1, Z1, X2, Y2, Z2)) LRN.findAll(X1, Y1, Z1, X2, Y2, Z2, ret);
            if (overlap(LRF, X1, Y1, Z1, X2, Y2, Z2)) LRF.findAll(X1, Y1, Z1, X2, Y2, Z2, ret);
        }

        private List<CoordHolder> findNNearestNeighbors(final double x, final double y, final double z, int n, Expression filter, Model model)
        {
            List<CoordHolder> ret = new LinkedList<>();
            List<CoordHolder> tmp = new LinkedList<>();
            Oct near = narrowDown(this, new CoordHolder(x, y, z, null, null));
            while(ret.size() + tmp.size() < n)
            {
                if (near.parent != null)
                {
                    near = near.parent;
                }
                collectAll(near, ret);
                //todo:add from parent's parent's adjacent octs... if it goes beyond that it gets too complicated and must be redone, forsaking these
                Iterator<CoordHolder> it = ret.iterator();
                while(it.hasNext())
                {
                    CoordHolder h = it.next();
                    if (h.o instanceof OutputShape)
                    {
                        OutputShape nb = (OutputShape) h.o;
                        //if (filter) it.remove();
                    }
                }
                tmp.addAll(ret);
                if (near.parent == null)
                    break;
                ret = new LinkedList<>();
            }
            ret = tmp;
            
            Collections.sort(ret, new Comparator<CoordHolder>() {
                @Override
                public int compare(CoordHolder a, CoordHolder b) {
                    double x0 = (x - a.x),
                            y0 = (y - a.y),
                            z0 = (z - a.z);
                    double dista = x0*x0 + y0*y0 + z0*z0;
                    x0 = (x - b.x);
                    y0 = (y - b.y);
                    z0 = (z - b.z);
                    double distb = x0*x0 + y0*y0 + z0*z0;
                    if (dista < distb) { return -1; }
                    if (dista > distb) { return 1; }
                    return 0;
                }
            });
            return ret.subList(0, Math.min(n, ret.size()));
        }
        
        public void collectAll(Oct container, List<CoordHolder> ret)
        {
            if (ULN == null)
            {
                ret.addAll(container.items);
                return;
            }
            collectAll(ULN, ret);  collectAll(ULF, ret);
            collectAll(URN, ret);  collectAll(URF, ret);
            collectAll(LLN, ret);  collectAll(LLF, ret);
            collectAll(LRN, ret);  collectAll(LRF, ret);
        }

        private boolean overlap(Oct q, double X1, double Y1, double Z1, double X2, double Y2, double Z2)
        {
            if (q.x2 < X1 || q.y2 < Y1 || q.z2 < Z1 || q.x1 > X2 || q.y1 > Y2 || q.z1 > Z2) return false;
            return true;
        }

        public CoordHolder place(double x, double y, double z, T o)
        {
            CoordHolder h = place_(new CoordHolder(x, y, z, o, null), 0);
            dirty = 0;
            return h;
        }
        
        public CoordHolder place(CoordHolder h, int n)
        {
            h = place_(h, n);
            dirty = 0;
            return h;
        }

        private CoordHolder place_(CoordHolder h, int n)
        {
            double x = h.x,
                    y = h.y,
                    z = h.z
                    ;
            if (x < x1 || y < y1 || z < z1 || x > x2 || y > y2 || z > z2) {
                if (x1 == x2)
                {
                    x1 = Math.min(x1, x);
                    y1 = Math.min(y1, y);
                    z1 = Math.min(z1, z);
                    double add = 
                            Math.max(
                                Math.max(Math.max(x2, x) - x1, Math.max(y2, y) - y1),
                                Math.max(z2, z) - z2
                            );
                    x2 = x1 + add;
                    y2 = y1 + add;
                    z2 = z1 + add;
                }
                else
                {
                    if (parent == null)
                    {
                        initParent(x, y, z);
                    }
                    return parent.place_(h, n+1);
                }
            }
            if (items.size() >= LEAF_MAX_OBJECTS && dirty == 0)
            {
                expand(n+1);
            }
            if (ULN != null)
            {
                return place_(h, this, n+1);
            }
            else
            {
                h.oct = this;
                items.add(h);
                return h;
            }
        }

        private CoordHolder place_(CoordHolder h, Oct oct, int n)
        {
            oct = narrowDown(oct, h);
            return oct.place_(h, n+1);
        }

        private Oct narrowDown(Oct oct, CoordHolder h)
        {
            while (oct.ULN != null)
            {
                if (h.x <= (oct.x2 + oct.x1) / 2)
                {
                    if (h.y <= (oct.y2 + oct.y1) / 2)
                    {
                        oct = (h.z <= (oct.z2 + oct.z1) / 2 ? oct.ULN : oct.ULF);
                    }
                    else
                    {
                        oct = (h.z <= (oct.z2 + oct.z1) / 2 ? oct.LLN : oct.LLF);
                    }
                }
                else
                {
                    if (h.y <= (oct.y2 + oct.y1) / 2)
                    {
                        oct = (h.z <= (oct.z2 + oct.z1) / 2 ? oct.URN : oct.URF);
                    }
                    else
                    {
                        oct = (h.z <= (oct.z2 + oct.z1) / 2 ? oct.LRN : oct.LRF);
                    }
                }
            }
            return oct;
        }
        
        private void expand(int n)
        {
            if (LLN == null)
            {
                dirty++;
                initOct();
            }
            for(CoordHolder c : items)
            {
                place_(c, this, n+1);
            }
            items = Collections.EMPTY_LIST;
        }

        private void initOct()
        {
            double x_ = (x2 + x1) / 2.0;
            double y_ = (y2 + y1) / 2.0;
            double z_ = (z2 + z1) / 2.0;
            ULN = new Oct(this, x1, y1, z1, x_, y_, z_);
            ULF = new Oct(this, x1, y1, z_, x_, y_, z2);
            URN = new Oct(this, x_, y1, z1, x2, y_, z_);
            URF = new Oct(this, x_, y1, z_, x2, y_, z2);
            LLN = new Oct(this, x1, y_, z1, x_, y2, z_);
            LLF = new Oct(this, x1, y_, z_, x_, y2, z2);
            LRN = new Oct(this, x_, y_, z1, x2, y2, z_);
            LRF = new Oct(this, x_, y_, z_, x2, y2, z2);
        }

        private Oct initParent(double x, double y, double z)
        {
            int octInd = 0;
            double
                    X1 = x1,
                    Y1 = y1,
                    Z1 = z1,
                    X2 = x2 + (x2 - x1),
                    Y2 = y2 + (y2 - y1),
                    Z2 = z2 + (z2 - z1)
                    ;
            if (x < X1)
            {
                octInd++;
                X1 -= (x2 - x1);
                X2 -= (x2 - x1);
            }
            if (y < Y1)
            {
                octInd += 2;
                Y1 -= (y2 - y1);
                Y2 -= (y2 - y1);
            }
            if (z < Z1)
            {
                octInd +=4;
                Z1 -= (z2 - z1);
                Z2 -= (z2 - z1);
            }
            parent = new Oct(null, X1, Y1, Z1, X2, Y2, Z2);
            parent.initOct();
            switch(octInd) {
                case 0: parent.ULN = this; break;
                case 1: parent.URN = this; break;
                case 2: parent.LLN = this; break;
                case 3: parent.LRN = this; break;
                case 4: parent.ULF = this; break;
                case 5: parent.URF = this; break;
                case 6: parent.LLF = this; break;
                case 7: parent.LRF = this; break;

            }
            root = parent;
            return parent;
        }

        public void replace(CoordHolder item, int n)
        {
            if (item.x < x1 || item.y < y1 || item.z < z1 || item.x > x2 || item.y > y2 || item.z > z2)
            {
                items.remove(item);
                if (parent == null)
                {
                    initParent(item.x, item.y, item.z);
                }
                parent.place_(item, n+1);
            }
        }

        private void printChar(PrintStream out, char c, int n)
        {
            for(int i = 0; i < n; i++) out.print(c);
        }

        private void print(PrintStream out, int indent)
        {
            printChar(out, '.', indent - 1); out.print(' ');
            out.print('(');
            out.print(x1);  out.print(',');  out.print(y1);  out.print(',');  out.print(z1);
            out.print(" - ");
            out.print(x2);  out.print(',');  out.print(y2);  out.print(',');  out.print(z2);
            out.println(')');

            for(CoordHolder h : this.items)
            {
                printChar(out, ' ', indent + 2);
                out.print(h.x);
                out.print(',');
                out.print(h.y);
                out.print(',');
                out.print(h.z);
                out.print(": ");
                out.println(h.o.toString().replaceAll("\r?\n.*", ""));
            }
            if (ULN != null)
            {
                printChar(out, ' ', indent);
                out.println('[');
                ULN.print(out, indent + 2);
                URN.print(out, indent + 2);
                LLN.print(out, indent + 2);
                LRN.print(out, indent + 2);
                ULF.print(out, indent + 2);
                URF.print(out, indent + 2);
                LLF.print(out, indent + 2);
                LRF.print(out, indent + 2);
                printChar(out, ' ', indent);
                out.println(']');
            }
        }

    }

    public Oct root;

    public List<CoordHolder> findAll(double x1, double y1, double z1, double x2, double y2, double z2)
    {
        if (root == null)
            return Collections.EMPTY_LIST;
        List<CoordHolder> ret = new ArrayList<CoordHolder>();
        root.findAll(x1, y1, z1, x2, y2, z2, ret);
        return ret;
    }

    public List<CoordHolder> findAll(double x, double y, double z, double radius)
    {
        List<Octree<T>.CoordHolder> list = findAll(
                x - radius, y - radius, z - radius, 
                x + radius, y + radius, z + radius)
                ;
        double r2 = radius * radius;
        if (list instanceof LinkedList)
        {
            //LinkedList<Octree<T>.CoordHolder> ll = (LinkedList)list;
            Iterator<Octree<T>.CoordHolder> it = list.iterator();
            while(it.hasNext())
            {
                Octree<T>.CoordHolder holder = it.next();
                double dist = 
                        Math.pow(holder.x - x, 2)
                        + Math.pow(holder.y - y, 2)
                        + Math.pow(holder.z - z, 2)
                        ;
                if (dist > r2)
                {
                    it.remove();
                }
            }
            return list;
        }
        List<CoordHolder> ret = new ArrayList<CoordHolder>(list.size());
        for(CoordHolder holder : list)
        {
            double dist = 
                    Math.pow(holder.x - x, 2)
                    + Math.pow(holder.y - y, 2)
                    + Math.pow(holder.z - z, 2)
                    ;
            if (dist <= r2)
            {
                ret.add(holder);
            }
        }
        return ret;
    }

    public List<CoordHolder> findNNearestNeighbors(double x, double y, double z, int n, Expression filter, Model model) {
        return root.findNNearestNeighbors(x, y, z, n, filter, model);
    }


    
    public CoordHolder place(double x, double y, double z, T o)
    {
        if (root == null)
        {
            root = new Oct(null, x, y, z, x, y, z);
        }
        CoordHolder h = root.place(x, y, z, o);
        size++;
        if (DYNAMIC_MAX_OBJECTS && size % 100 == 0)
        {
            adjustMaxObjects();
        }
        return h;
    }

    public void adjustMaxObjects()
    {
        this.LEAF_MAX_OBJECTS = Math.max(7,
                (int)Math.pow(size, MAX_OBJ_TARGET_EXPONENT));
    }

    public int size() { return size; }

    public void print(PrintStream out)
    {
        root.print(out, 0);
    }

}
