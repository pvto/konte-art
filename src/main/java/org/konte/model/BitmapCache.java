/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.konte.model;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.WeakHashMap;
import sun.awt.image.ToolkitImage;

/**
 *
 * @author pto
 */
public class BitmapCache {

    private static HashMap<String, Image> cache = new HashMap<String, Image>();
    
    private HashMap<String, Image> references = new HashMap<String, Image>();

    public Image[] imageArr;

    public String getKey(File fl) {
        return fl.getAbsolutePath() + "_" + fl.lastModified();
    }

    public Image add(Object src, String reference) throws Exception {
        String key = null;
        if (src instanceof File)
            key = getKey((File)src);
        else if (src instanceof URL)
            key = src.toString();
        else
            throw new IllegalArgumentException("Expecting File of URL, found: " + src.getClass());
        Image img = cache.get(key);
        if (img == null) {
            if (src instanceof File)
                img = loadImage((File)src);
            else if (src instanceof URL)
                img = loadImage((URL)src);
            if (img == null) {
                throw new Exception("Image not found");
            }
            if (img instanceof ToolkitImage) {
                ((ToolkitImage)img).preload(null);
                BufferedImage bim = null;
                while(bim == null)
                    bim = ((ToolkitImage)img).getBufferedImage();
                img = bim;
            }
            cache.put(key, img);
        }
        references.put(reference, img);
        return img;
    }

    private Image loadImage(File fl) {
        Image img = null;
        while (img == null || img.getWidth(null) <= 0) {
            img = Toolkit.getDefaultToolkit().getImage(fl.getAbsolutePath());
        }
        return img;
    }

    private Image loadImage(URL url) {
        Image img = null;
        while (img == null || img.getWidth(null) <= 0) {
            img = Toolkit.getDefaultToolkit().getImage(url);
        }
        return img;
    }

    public Image getImage(String reference) {
        return references.get(reference);
    }

    public int getIndex(String reference) {
        Image img = references.get(reference);
        if (img == null)
            return -1;
        for(int i = 0; i < imageArr.length; i++) {
            if (imageArr[i] == img)
                return i;
        }
        return -1;
    }
    
    public void init() {
        imageArr = new Image[references.size()];
        int i = 0;
        for(Image img : references.values()) {
            imageArr[i++] = img;
        }
    }

    public void clearReferences() {
        for(String s : references.keySet())
            references.get(s).flush();
        references = new HashMap<String, Image>();
    }

    public static void clearCache() {
        cache.clear();
    }
}
