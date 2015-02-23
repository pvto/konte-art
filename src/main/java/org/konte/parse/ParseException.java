
package org.konte.parse;

/**
 *
 * @author pvto
 */
public class ParseException extends Exception {

    private int lineNr;
    private int caretPos;
    
    public ParseException(String message)
    {
        super(message);
    }

    public ParseException(String message, int lineNr, int caretPos)
    {
        super(message);
        this.caretPos = caretPos;
        this.lineNr = lineNr;
    }

    
    
    public int getCaretPos()
    {
        return caretPos;
    }

    public int getLineNr()
    {
        return lineNr;
    }


    @Override
    public String getMessage()
    {
        return /*lineNr + ":" + caretPos + " " + */
                super.getMessage()/*.replaceAll("^\\d+:\\d+ ", " ")*/;
    }

    
    
}
