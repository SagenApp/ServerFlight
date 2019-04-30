package app.sagen.serverflight.util;

public class CacheItem {
    float position;
    float xpos, ypos, zpos;
    float travelled;

    public CacheItem(float xpos, float ypos, float zpos) {
        this.xpos = xpos;
        this.ypos = ypos;
        this.zpos = zpos;
    }
}
