package ecnu.cs14.garagelocation;

import android.util.Pair;
import ecnu.cs14.garagelocation.data.Fingerprint;
import ecnu.cs14.garagelocation.data.Map;

/**
 * An abstract locating algorithm.
 * Created by K on 2017/2/11.
 */

public abstract class Algorithm {
    public Algorithm(Map map) {

    }
    public abstract Pair<Integer, Integer> locate(Fingerprint fingerprint);
    public abstract void close() throws Exception;
}
