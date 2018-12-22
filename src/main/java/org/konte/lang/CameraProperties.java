
package org.konte.lang;

public enum CameraProperties {
    SIMPLE(0),
    PANNING(16),
    ORTOGRAPHIC(32),
    CIRCULAR(64),
    CABINET(128),
    FISH(256), // fishy fish eye simulation
    FISHEYE(512), // real fish eye optics
    ZPOW(1024),
    BEZIER2(2048),
    STEREOGRAPHIC(4096),
    AZIMUTHAL(8192)
    ;

    public int ENC;

    private CameraProperties(int enc) {
        this.ENC = enc;
    }
}
