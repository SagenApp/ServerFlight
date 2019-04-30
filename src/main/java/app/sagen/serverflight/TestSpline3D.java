package app.sagen.serverflight;

import app.sagen.serverflight.util.Spline3D;

import java.util.Arrays;

public class TestSpline3D {

    public static void main(String[] args) {
        float[][] points = new float[][]{
                {0, 0, 0},
                {0, 10, 10},
                {0, 10, 0}
        };
        Spline3D spline3D = new Spline3D(points, 5f, 0.01f);

        for (double step = 0; step <= 25; step += 0.05) {
            float[] currentPosition = spline3D.getTripPosition((float) step);
            System.out.println(String.format("%1$25s\t", step) + Arrays.toString(currentPosition));
        }
    }

}
