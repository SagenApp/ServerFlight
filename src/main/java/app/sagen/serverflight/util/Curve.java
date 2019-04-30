package app.sagen.serverflight.util;

public class Curve {

    static final Cubic[] calcCurve(int n, float[] axis) {
        float[] gamma = new float[n + 1];
        float[] delta = new float[n + 1];
        float[] d = new float[n + 1];
        Cubic[] c = new Cubic[n];

        // gamma
        gamma[0] = 0.5F;
        for (int i = 1; i < n; i++)
            gamma[i] = 1.0F / (4.0F - gamma[i - 1]);
        gamma[n] = 1.0F / (2.0F - gamma[n - 1]);

        // delta
        delta[0] = 3.0F * (axis[1] - axis[0]) * gamma[0];
        for (int i = 1; i < n; i++)
            delta[i] = (3.0F * (axis[i + 1] - axis[i - 1]) - delta[i - 1])
                    * gamma[i];
        delta[n] = (3.0F * (axis[n] - axis[n - 1]) - delta[n - 1])
                * gamma[n];

        // d
        d[n] = delta[n];
        for (int i = n - 1; i >= 0; i--)
            d[i] = delta[i] - gamma[i] * d[i + 1];

        // c
        for (int i = 0; i < n; i++) {
            float x0 = axis[i];
            float x1 = axis[i + 1];
            float d0 = d[i];
            float d1 = d[i + 1];
            c[i] = new Cubic(x0, d0, 3.0F * (x1 - x0) - 2.0F * d0 - d1,
                    2.0F * (x0 - x1) + d0 + d1);
        }
        return c;
    }
}
