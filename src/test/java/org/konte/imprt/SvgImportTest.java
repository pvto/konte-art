package org.konte.imprt;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import org.junit.Test;

public class SvgImportTest {
    
    String svg = 
"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"+
"<!-- Created with Inkscape (http://www.inkscape.org/) -->"+
""+
"<svg"+
"   xmlns:dc=\"http://purl.org/dc/elements/1.1/\""+
"   xmlns:cc=\"http://creativecommons.org/ns#\""+
"   xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\""+
"   xmlns:svg=\"http://www.w3.org/2000/svg\""+
"   xmlns=\"http://www.w3.org/2000/svg\""+
"   xmlns:sodipodi=\"http://sodipodi.sourceforge.net/DTD/sodipodi-0.dtd\""+
"   xmlns:inkscape=\"http://www.inkscape.org/namespaces/inkscape\""+
"   width=\"744.09448819\""+
"   height=\"1052.3622047\""+
"   id=\"svg2\""+
"   version=\"1.1\""+
"   inkscape:version=\"0.48.4 r9939\""+
"   sodipodi:docname=\"New document 1\">"+
"  <defs"+
"     id=\"defs4\" />"+
"  <sodipodi:namedview"+
"     id=\"base\""+
"     pagecolor=\"#ffffff\""+
"     bordercolor=\"#666666\""+
"     borderopacity=\"1.0\""+
"     inkscape:pageopacity=\"0.0\""+
"     inkscape:pageshadow=\"2\""+
"     inkscape:zoom=\"0.7\""+
"     inkscape:cx=\"375\""+
"     inkscape:cy=\"675.71429\""+
"     inkscape:document-units=\"px\""+
"     inkscape:current-layer=\"layer1\""+
"     showgrid=\"false\""+
"     inkscape:window-width=\"1301\""+
"     inkscape:window-height=\"744\""+
"     inkscape:window-x=\"65\""+
"     inkscape:window-y=\"24\""+
"     inkscape:window-maximized=\"1\" />"+
"  <metadata"+
"     id=\"metadata7\">"+
"    <rdf:RDF>"+
"      <cc:Work"+
"         rdf:about=\"\">"+
"        <dc:format>image/svg+xml</dc:format>"+
"        <dc:type"+
"           rdf:resource=\"http://purl.org/dc/dcmitype/StillImage\" />"+
"        <dc:title></dc:title>"+
"      </cc:Work>"+
"    </rdf:RDF>"+
"  </metadata>"+
"  <g"+
"     inkscape:label=\"Layer 1\""+
"     inkscape:groupmode=\"layer\""+
"     id=\"layer1\">"+
"    <g"+
"       style=\"font-size:1047.35522461px;font-style:normal;font-weight:normal;line-height:125%;letter-spacing:0px;word-spacing:0px;fill:#000000;fill-opacity:1;stroke:none;font-family:Sans\""+
"       id=\"text2985\">"+
"      <path"+
"         d=\"m 742.88303,722.74042 0,-49.2257 -24.08917,0 c -51.32036,0 -54.46249,-5.23679 -63.88867,-19.89975 L 416.1082,279.70916 613.01098,85.948441 C 642.3369,57.669878 645.47907,53.480429 728.22005,53.480429 l 0,-49.2256956 c -31.42062,2.0947084 -64.93605,3.1420657 -96.35668,3.1420657 -39.79946,0 -97.40407,-3.2e-6 -136.15618,-3.1420657 l 0,49.2256956 c 17.80502,0 40.84688,10e-6 67.03074,10.473552 -3.14207,4.189417 -3.14207,6.284137 -9.4262,11.520908 l -303.73301,299.543591 0,-321.538051 98.45139,0 0,-49.2256956 c -36.6574,3.1420625 -130.91945,3.1420657 -172.81362,3.1420657 -41.89416,0 -136.156211,-3.2e-6 -172.8136076,-3.1420657 l 0,49.2256956 98.4513876,0 0,620.034291 -98.4513876,0 0,49.2257 c 36.6573966,-3.14207 130.9194476,-3.14207 172.8136076,-3.14207 41.89417,0 136.15622,0 172.81362,3.14207 l 0,-49.2257 -98.45139,0 0,-229.37079 71.22015,-70.1728 175.95568,276.50178 c 6.28412,8.37883 6.28413,11.5209 6.28413,11.5209 0,11.5209 -45.0363,11.52091 -67.03073,11.52091 l 0,49.2257 c 36.65739,-3.14207 125.68266,-3.14207 166.52948,-3.14207 45.03623,0 95.30937,0 140.3456,3.14207\""+
"         style=\"font-variant:normal;font-weight:600;font-stretch:normal;text-align:start;line-height:125%;writing-mode:lr-tb;text-anchor:start;font-family:Latin Modern Roman Demi;-inkscape-font-specification:Latin Modern Roman Demi Semi-Bold\""+
"         id=\"path2990\" />"+
"    </g>"+
"  </g>"+
"</svg>"
;
/*
m 742.88303,722.74042 0,-49.2257 -24.08917,0 
    c -51.32036,0 -54.46249,-5.23679 -63.88867,-19.89975 
    L 416.1082,279.70916 613.01098,85.948441 
    C 642.3369,57.669878 645.47907,53.480429 728.22005,53.480429 
    l 0,-49.2256956 
    c -31.42062,2.0947084 -64.93605,3.1420657 -96.35668,3.1420657 -39.79946,0 -97.40407,-3.2e-6 -136.15618,-3.1420657 
    l 0,49.2256956 
    c 17.80502,0 40.84688,10e-6 67.03074,10.473552 -3.14207,4.189417 -3.14207,6.284137 -9.4262,11.520908 
    l -303.73301,299.543591 0,-321.538051 98.45139,0 0,-49.2256956 
    c -36.6574,3.1420625 -130.91945,3.1420657 -172.81362,3.1420657 -41.89416,0 -136.156211,-3.2e-6 -172.8136076,-3.1420657 
    l 0,49.2256956 98.4513876,0 0,620.034291 -98.4513876,0 0,49.2257 
    c 36.6573966,-3.14207 130.9194476,-3.14207 172.8136076,-3.14207 41.89417,0 136.15622,0 172.81362,3.14207 
    l 0,-49.2257 -98.45139,0 0,-229.37079 71.22015,-70.1728 175.95568,276.50178 
    c 6.28412,8.37883 6.28413,11.5209 6.28413,11.5209 0,11.5209 -45.0363,11.52091 -67.03073,11.52091 
    l 0,49.2257 
    c 36.65739,-3.14207 125.68266,-3.14207 166.52948,-3.14207 45.03623,0 95.30937,0 140.3456,3.14207    
*/
    @Test
    public void testImport() throws Exception {
        SvgImport i = new SvgImport();
        i.initDocument(new ByteArrayInputStream(svg.getBytes("UTF-8")));
        //i.getPaths().item(0)
        
        String res = i.allPathsToScript("a", new HashMap());
        System.out.println(res);
    }
    
}
