package app.sagen.serverflight.util;

import lombok.Data;

@Data
public class Vertex {
    String name;
    float x;
    float y;
    float z;
    boolean teleportable;

    public Vertex(String name, float x, float y, float z, boolean teleportable) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.teleportable = teleportable;
    }

    public float heristic(Vertex vertex) {
        // sum of each axis differance to make it prefer closer targets
        return (float) Math.abs(this.x - vertex.x) + Math.abs(this.y - vertex.y) + Math.abs(this.z - vertex.z);
        // return (float) Math.sqrt(Math.pow(this.x - vertex.x, 2) + Math.pow(this.y - vertex.y, 2) + Math.pow(this.z - vertex.z, 2));
    }

    public float heristic(float x, float y, float z) {
        // sum of each axis differance to make it prefer closer targets
        return (float) Math.abs(this.x - x) + Math.abs(this.y - y) + Math.abs(this.z - z);
        //return (float) Math.sqrt(Math.pow(this.x - x, 2) + Math.pow(this.y - y, 2) + Math.pow(this.z - z, 2));
    }
}
