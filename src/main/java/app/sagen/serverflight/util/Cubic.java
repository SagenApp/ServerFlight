package app.sagen.serverflight.util;

public class Cubic {
    private final float a, b, c, d;

    Cubic(float a, float b, float c, float d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    final float eval(float u) {
        return (((d * u) + c) * u + b) * u + a;
    }
}
