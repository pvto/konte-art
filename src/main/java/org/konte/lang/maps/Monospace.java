package org.konte.lang.maps;

import org.konte.misc.PrefixStringMap;

/**
 *
 * @author Paavo Toivanen https://github.com/pvto
 */
public class Monospace extends PrefixStringMap {

    private static final String[] MONOSPACE_EXCEPTIONS = new String[] {
        " ", "m_",
        "!", "m_excl",
        "\"", "m_quot",
        "#", "m_hash",
        "$", "m_dollar",
        "%", "m_percent",
        "&", "m_et",
        "'", "m_apos",
        "(", "m_lb",
        ")", "m_rb",
        "*", "m_aster",
        "+", "m_plus",
        ",", "m_comma",
        "-", "m_dash",
        ".", "m_stop",
        "/", "m_slash",
        ":", "m_colon",
        ";", "m_semicolon",
        "<", "m_less",
        "=", "m_equals",
        ">", "m_greater",
        "?", "m_question",
        "@", "m_at",
        "[", "m_lsqb",
        "\\", "m_backslash",
        "]", "m_rsqb",
        "^", "m_circum",
        "_", "m_lodash",
        "`", "m_grave",
        "{", "m_lcb",
        "|", "m_pipe",
        "}", "m_rcb",
        "~", "m_tilde",
        "Å", "mAO",
        "Ä", "mAE",
        "Ö", "mOE",
        "å", "mao",
        "ä", "mae",
        "ö", "moe"
        
    };
    
    private Monospace() {
        this("m", MONOSPACE_EXCEPTIONS);
    }
    private Monospace(String prefix, String[] exceptions) {
        super(prefix, exceptions);
    }
    
    public static final Monospace monospace = new Monospace();

}
