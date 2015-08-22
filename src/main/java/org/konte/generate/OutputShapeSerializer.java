package org.konte.generate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.konte.image.OutputShape;
import org.konte.lang.Language;
import org.konte.misc.Matrix4;
import org.konte.misc.Serializer;

public class OutputShapeSerializer implements Serializer {

    @Override
    public byte[] marshal(Object o) throws IOException
    {
        OutputShape p = (OutputShape)o;
        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(ba);

        out.writeFloat(p.matrix.m00);  out.writeFloat(p.matrix.m01);  out.writeFloat(p.matrix.m02);  out.writeFloat(p.matrix.m03);
        out.writeFloat(p.matrix.m10);  out.writeFloat(p.matrix.m11);  out.writeFloat(p.matrix.m12);  out.writeFloat(p.matrix.m13);
        out.writeFloat(p.matrix.m20);  out.writeFloat(p.matrix.m21);  out.writeFloat(p.matrix.m22);  out.writeFloat(p.matrix.m23);
        out.writeFloat(p.matrix.m33);
        out.writeInt(p.col);
        out.writeFloat(p.layer);
        out.writeShort(p.fov);
        out.writeInt(p.shape.getId());

        return ba.toByteArray();
    }

    @Override
    public Object unmarshal(byte[] bytes) throws IOException
    {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
        OutputShape p = new OutputShape();
        p.matrix = new Matrix4();
        p.matrix.m00 = in.readFloat(); p.matrix.m01 = in.readFloat(); p.matrix.m02 = in.readFloat(); p.matrix.m03 = in.readFloat();
        p.matrix.m10 = in.readFloat(); p.matrix.m11 = in.readFloat(); p.matrix.m12 = in.readFloat(); p.matrix.m13 = in.readFloat();
        p.matrix.m20 = in.readFloat(); p.matrix.m21 = in.readFloat(); p.matrix.m22 = in.readFloat(); p.matrix.m23 = in.readFloat();
        p.matrix.m33 = in.readFloat();
        p.col = in.readInt();
        p.layer = in.readFloat();
        p.fov = in.readShort();
        p.shape = Language.getUntransformable(in.readInt());
        return p;
    }

    @Override
    public int objectSize(Object o)
    {
        return 4*4*3+4 + 4 + 4 + 2 + 4;
    }

}
