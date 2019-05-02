package app.sagen.serverflight.util;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Spline3D {
    private final int count;
    private final Cubic[] x, y, z;
    private List<CacheItem> travelCache;
    private float maxTravelStep;
    private float posStep;

    public Spline3D(float[][] points, float cacheMaxTravel, float cachePosStep) {
        count = points.length;

        float[] x = new float[count];
        float[] y = new float[count];
        float[] z = new float[count];

        for (int i = 0; i < count; i++) {
            x[i] = points[i][0];
            y[i] = points[i][1];
            z[i] = points[i][2];
        }

        this.x = calcCurve(count - 1, x);
        this.y = calcCurve(count - 1, y);
        this.z = calcCurve(count - 1, z);

        enabledTripCaching(cacheMaxTravel, cachePosStep); // enable cache
    }

    public static Cubic[] calcCurve(int n, float[] axis) {
        float[] gamma = new float[n + 1];
        float[] delta = new float[n + 1];
        float[] d = new float[n + 1];
        Cubic[] c = new Cubic[n];

        // gamma
        gamma[0] = 0.5F;
        for (int i = 1; i < n; i++) {
            gamma[i] = 1.0F / (4.0F - gamma[i - 1]);
        }
        gamma[n] = 1.0F / (2.0F - gamma[n - 1]);

        // delta
        delta[0] = 3.0F * (axis[1] - axis[0]) * gamma[0];
        for (int i = 1; i < n; i++) {
            delta[i] = (3.0F * (axis[i + 1] - axis[i - 1]) - delta[i - 1]) * gamma[i];
        }
        delta[n] = (3.0F * (axis[n] - axis[n - 1]) - delta[n - 1]) * gamma[n];

        // d
        d[n] = delta[n];
        for (int i = n - 1; i >= 0; i--) {
            d[i] = delta[i] - gamma[i] * d[i + 1];
        }

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

    private static float dist(float[] a, float[] b) {
        float dx = b[0] - a[0];
        float dy = b[1] - a[1];
        float dz = b[2] - a[2];

        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public final int pointCount() {
        return count;
    }

    public final float[] getPositionAt(float param) {
        float[] v = new float[3];
        this.getPositionAt(param, v);
        return v;
    }

    public final void getPositionAt(float param, float[] result) {
        // clamp
        if (param < 0.0f) {
            param = 0.0f;
        }
        if (param >= count - 1) {
            param = (count - 1) - Math.ulp(count - 1);
        }

        // split
        int ti = (int) param;
        float tf = param - ti;

        // eval
        result[0] = x[ti].eval(tf);
        result[1] = y[ti].eval(tf);
        result[2] = z[ti].eval(tf);
    }

    public float getTotalTripDistance() {
        return travelCache.get(travelCache.size() - 1).getTravelled();
    }

    public float[] getTripPosition(float totalTrip) {
        CacheItem last = this.travelCache.get(this.travelCache.size() - 1);

        // build cache
        while (last.travelled < totalTrip) {
            if (totalTrip == 0.0f) {
                break;
            }

            float travel = Math.min(totalTrip - last.travelled, maxTravelStep);

            CacheItem curr = this.getSteppingPosition(last.position, travel, posStep);

            if (curr.position >= this.count) {
                // reached end of spline
                break;
            }

            // only cache if we travelled far enough
            if (curr.travelled > this.maxTravelStep * 0.95f) {
                this.travelCache.add(curr);
            }

            curr.travelled += last.travelled;

            last = curr;
        }

        // find closest cache item with binary search
        int lo = 0;
        int hi = this.travelCache.size() - 1;
        while (true) {
            int mid = (lo + hi) / 2;

            last = this.travelCache.get(mid);

            if (last.travelled < totalTrip) {
                if (lo == mid) break;
                lo = mid;
            } else {
                if (hi == mid) break;
                hi = mid;
            }
        }

        for (int i = lo; i <= hi; i++) {
            CacheItem item = this.travelCache.get(i);

            if (item.travelled <= totalTrip) {
                last = item;
            } else {
                break;
            }
        }

        float travel = totalTrip - last.travelled;
        last = this.getSteppingPosition(last.position, travel, posStep);

        return new float[]{
                last.xpos,
                last.ypos,
                last.zpos
        };
    }

    private void enabledTripCaching(float maxTravelStep, float posStep) {
        this.maxTravelStep = maxTravelStep;
        this.posStep = posStep;

        float x = this.x[0].eval(0.0f);
        float y = this.y[0].eval(0.0f);
        float z = this.z[0].eval(0.0f);

        this.travelCache = new ArrayList<>();
        this.travelCache.add(new CacheItem(x, y, z));

        this.getTripPosition(50000); // force full caching
    }

    private CacheItem getSteppingPosition(float posOffset, float travel, float segmentStep) {
        float pos = posOffset;
        float[] last = this.getPositionAt(pos);

        float travelled = 0.0f;

        while (travelled < travel && pos < this.count) {
            float[] curr = this.getPositionAt(pos += segmentStep);
            travelled += Spline3D.dist(last, curr);
            last = curr;
        }

        CacheItem item = new CacheItem(last[0], last[1], last[2]);
        item.position = pos;
        item.travelled = travelled;
        return item;
    }
}
