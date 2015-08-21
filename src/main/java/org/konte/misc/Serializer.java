package org.konte.misc;

import java.io.IOException;

public interface Serializer {

    byte[] marshal(Object o) throws IOException;
        Object unmarshal(byte[] bytes) throws IOException;
        int objectSize(Object o);
    
}
