package org.konte.model;

import java.awt.image.BufferedImage;
import org.konte.expression.Name;
import org.konte.expression.NameBackReference;
import org.konte.lang.Tokens.InnerExpressiveToken;
import org.konte.lang.Language;
import org.konte.lang.Tokens.Token;
import org.konte.parse.ParseException;

/**Creates expressions that backreference a name in the model.
 *
 * @author pvto
 */
public class BackReferenceFactory {

    private static int getRgb(Model modl, Token token) throws ParseException
    {
        DrawingContext o = modl.context;
        int img = (int)o.getDef(modl.imgIndex);
        int u = (int)o.getDef(modl.imguIndex);
        try
        {
            int v = (int)o.getDef(modl.imgvIndex);
            int rgb = ((BufferedImage)modl.bitmapCache.imageArr[img]).getRGB(u, v);
            return rgb;
        }
        catch(ArrayIndexOutOfBoundsException ex)
        {
            throw new ParseException("error evaluating '" + token + "': img not matching to any definition");
        }
    }

    public static Name newBackReference(final Token token, final Model modl) throws ParseException
    {
        if (token == Language.imgr)
        {
            return new NameBackReference()
            {
                @Override public Float evaluate() throws ParseException
                {
                    int rgb = getRgb(modl, token);
                    return (float)((rgb >> 16 ) & 0xFF) / 255f;
                }
            };
        } else if (token == Language.imgg)
        {
            return new NameBackReference()
            {
                @Override public Float evaluate() throws ParseException
                {
                    int rgb = getRgb(modl, token);
                    return (float)((rgb >> 8 ) & 0xFF) / 255f;
                }
            };
        } else if (token == Language.imgb)
        {
            return new NameBackReference()
            {
                @Override public Float evaluate() throws ParseException
                {
                    int rgb = getRgb(modl, token);
                    return (float)(rgb & 0xFF) / 255f;
                }
            };
        } else if (token == Language.imga)
        {
            return new NameBackReference()
            {
                @Override public Float evaluate() throws ParseException
                {
                    int rgb = getRgb(modl, token);
                    return (float)(rgb >> 24) / 255f;
                }
            };
        }
        return null;
    }
    public static Name newBackReference(InnerExpressiveToken matchingToken, String name, final Model modl) throws ParseException 
    {
        if (matchingToken == Language.depth)
        {
            return new NameBackReference()
            {
                @Override public Float evaluate() throws ParseException 
                {
                    return (float)modl.context.d;
                }
            };
        } else if (matchingToken == Language.x)
        {
            return new NameBackReference()
            {
                @Override public Float evaluate() throws ParseException 
                {
                    return modl.context.getx();
                }
            };
        } else if (matchingToken == Language.y)
        {
            return new NameBackReference()
            {
                @Override public Float evaluate() throws ParseException 
                {
                    return modl.context.gety();
                }
            };
        } else if (matchingToken == Language.z)
        {
            return new NameBackReference()
            {
                @Override public Float evaluate() throws ParseException 
                {
                    return modl.context.getz();
                }
            };
        } else if (matchingToken == Language.s)
        {
            return new NameBackReference()
            {
                @Override public Float evaluate() throws ParseException 
                {
                    return (modl.context.getsx()+modl.context.getsy()+modl.context.getsz()) / 3f;
                }
            };
        } else if (matchingToken == Language.sx)
        {
            return new NameBackReference()
            {
                @Override public Float evaluate() throws ParseException 
                {
                    return modl.context.getsx();
                }
            };
        } else if (matchingToken == Language.sy)
        {
            return new NameBackReference()
            {
                @Override public Float evaluate() throws ParseException 
                {
                    return modl.context.getsy();
                }
            };
        } else if (matchingToken == Language.sz)
        {
            return new NameBackReference()
            {
                @Override public Float evaluate() throws ParseException 
                {
                    return modl.context.getsz();
                }
            };
        } else if (matchingToken == Language.rx)
        {
            return new NameBackReference()
            {
                @Override public Float evaluate() throws ParseException 
                {
                    return modl.context.getrx();
                }
            };
        } else if (matchingToken == Language.ry)
        {
            return new NameBackReference()
            {
                @Override public Float evaluate() throws ParseException 
                {
                    return modl.context.getry();
                }
            };
        } else if (matchingToken == Language.rz)
        {
            return new NameBackReference()
            {
                @Override public Float evaluate() throws ParseException 
                {
                    return modl.context.getrz();
                }
            };
        } else if (matchingToken == Language.skewx)
        {
            return new NameBackReference()
            {
                @Override public Float evaluate() throws ParseException 
                {
                    return modl.context.getskewx();
                }
            };
        } else if (matchingToken == Language.skewy)
        {
            return new NameBackReference()
            {
                @Override public Float evaluate() throws ParseException 
                {
                    return modl.context.getskewy();
                }
            };
        } else if (matchingToken == Language.skewz)
        {
            return new NameBackReference()
            {
                @Override public Float evaluate() throws ParseException 
                {
                    return modl.context.getskewz();
                }
            };
        } else if (matchingToken == Language.R)
        {
            return new NameBackReference()
            {
                @Override public Float evaluate() throws ParseException 
                {
                    return modl.context.getR();
                }
            };
        } else if (matchingToken == Language.G)
        {
            return new NameBackReference()
            {
                @Override public Float evaluate() throws ParseException 
                {
                    return modl.context.getG();
                }
            };
        } else if (matchingToken == Language.B)
        {
            return new NameBackReference()
            {
                @Override public Float evaluate() throws ParseException 
                {
                    return modl.context.getB();
                }
            };
        } else if (matchingToken == Language.A)
        {
            return new NameBackReference()
            {
                @Override public Float evaluate() throws ParseException 
                {
                    return modl.context.getA();
                }
            };
        } else if (matchingToken == Language.shading)
        {
            return new NameBackReference()
            {
                @Override public Float evaluate() throws ParseException 
                {
                    return modl.context.getshading();
                }
            };
        } else if (matchingToken == Language.col0)
        {
            return new NameBackReference()
            {
                @Override public Float evaluate() throws ParseException 
                {
                    return modl.context.getcol0();
                }
            };
        } else if (matchingToken == Language.H)
        {
            return new NameBackReference()
            {
                @Override public Float evaluate() throws ParseException 
                {
                    return modl.context.getHue();
                }
            };
        } else if (matchingToken == Language.S)
        {
            return new NameBackReference()
            {
                @Override public Float evaluate() throws ParseException 
                {
                    return modl.context.getSat();
                }
            };
        } else if (matchingToken == Language.L)
        {
            return new NameBackReference()
            {
                @Override public Float evaluate() throws ParseException 
                {
                    return modl.context.getL();
                }
            };
        } else if (matchingToken == Language.layer)
        {
            return new NameBackReference()
            {
                @Override public Float evaluate() throws ParseException 
                {
                    return modl.context.getlayer();
                }
            };
/*        } else if (matchingToken == Language.img)
        {
            return new NameBackReference()
            {
                @Override public Float evaluate() throws ParseException 
                {
                    return modl.context.getBitmap();
                }
            };*/
        }
        else
        {
            throw new ParseException("Unconstruable inner token " + name + " in expression.");
        }

    }
}
