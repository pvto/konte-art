package org.konte.generate;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;
import java.util.WeakHashMap;


public class StateServer {

    private List<String> states;
    private WeakHashMap<String, Integer> history = new WeakHashMap<String, Integer>(10);
    private List<Observer> listeners = new ArrayList<Observer>();
    
    public StateServer()
    {
        states = new ArrayList<String>()
        {
            public boolean add(String str)
            {
                if (size() > 2)
                {
                    this.remove(0);
                }
                return super.add(str);
            }
        };
    }

    private int current = 0;
    
    public boolean addState(String state)
    {
        boolean ret;
        if (history.get(state) != null)
            return false;
        history.put(state, ++current);
        synchronized(states)
        {
            ret = states.add(state);
            if (ret)
            {
                for (int i = 0; i < listeners.size(); i++)
                {
                    listeners.get(i).update(null, state);
                }
            }
        }
        return ret;
    }
    
    public String getCurrentState()
    {
        synchronized(states)
        {
            return states.size() > 0 ?
                states.get(states.size()-1) :
                "waiting";
        }
    }

    public void clear()
    {
        history.clear();
        states.clear();
    }

    public void setListener(Observer o)
    {
        listeners.clear();
        listeners.add(o);
    }
    
}
