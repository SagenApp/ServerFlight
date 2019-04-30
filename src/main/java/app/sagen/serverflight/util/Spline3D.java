package app.sagen.serverflight.util;

import java.util.ArrayList;
import java.util.List;

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

        this.x = Curve.calcCurve(count - 1, x);
        this.y = Curve.calcCurve(count - 1, y);
        this.z = Curve.calcCurve(count - 1, z);

        enabledTripCaching(cacheMaxTravel, cachePosStep); // enable cache
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
        if (param < 0.0f)
            param = 0.0f;
        if (param >= count - 1)
            param = (count - 1) - Math.ulp(count - 1);

        // split
        int ti = (int) param;
        float tf = param - ti;

        // eval
        result[0] = x[ti].eval(tf);
        result[1] = y[ti].eval(tf);
        result[2] = z[ti].eval(tf);
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
                if (lo == mid)
                    break;
                lo = mid;
            } else {
                if (hi == mid)
                    break;
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
