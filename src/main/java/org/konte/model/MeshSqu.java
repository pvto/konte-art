
package org.konte.model;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import org.konte.image.Camera;
import org.konte.image.Canvas;
import org.konte.image.OutputShape;

/**A serializable shape (quadrilateral).
 *
 * @author pvto
 */
public class MeshSqu extends Untransformable implements Serializable {

    public float[][] coords;

    public MeshSqu(DrawingContext p1, DrawingContext p2, DrawingContext p3, DrawingContext p4)
    {
        coords = new float[][] {
            {p1.matrix.m03,p1.matrix.m13,p1.matrix.m23},
            {p2.matrix.m03,p2.matrix.m13,p2.matrix.m23},
            {p3.matrix.m03,p3.matrix.m13,p3.matrix.m23},
            {p4.matrix.m03,p4.matrix.m13,p4.matrix.m23}
        };
    }
    
    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        for(int i=0;i<4;i++) 
            for(int j=0;j<3;j++)
                out.writeFloat(coords[i][j]);
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        coords = new float[4][3];
        for(int i=0;i<4;i++) 
            for(int j=0;j<3;j++)
                coords[i][j] = in.readFloat();        
    }

    private void readObjectNoData()
            throws ObjectStreamException {

    }

    
    @Override
    public void draw(Camera camera, Canvas canvas, OutputShape shape)
    {
            canvas.drawMeshPiece(camera,shape);
    }
    
}
