package ecnu.cs14.garagelocation;

import android.util.Log;
import android.util.Pair;
import ecnu.cs14.garagelocation.data.Fingerprint;
import ecnu.cs14.garagelocation.data.Map;

/**
 * A dummy that helps debugging.
 * Created by K on 2017/2/11.
 */

public final class DummyAlgorithm extends Algorithm {
    private static final String TAG = DummyAlgorithm.class.getName();

    public DummyAlgorithm(Map map) {
        super(map);
        Log.d(TAG, "DummyAlgorithm: ctor called");
    }

    @Override
    public Pair<Integer, Integer> locate(Fingerprint fingerprint) {
        Log.d(TAG, "locate: called");
        return new Pair<>(0, 0);
    }

    @Override
    public void close() throws Exception {
        Log.d(TAG, "close: called");
    }
}
