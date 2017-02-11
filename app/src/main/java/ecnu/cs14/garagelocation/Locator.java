package ecnu.cs14.garagelocation;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import ecnu.cs14.garagelocation.data.Ap;
import ecnu.cs14.garagelocation.data.Fingerprint;
import ecnu.cs14.garagelocation.data.Map;
import ecnu.cs14.garagelocation.env.Environment;
import ecnu.cs14.garagelocation.info.SpaceInfo;

import java.util.HashSet;
import java.util.List;

/**
 * A locator using a substitutable algorithm.
 * Created by K on 2017/2/10.
 */

public final class Locator {
    private static final String TAG = Locator.class.getName();

    private Environment environment;
    private List<Map> maps;
    private int mapIndex;
    private Map map;
    private SpaceInfo spaceInfo;
    private Algorithm algorithm;

    /**
     * A time-consuming constructor.
     * @param context Context.
     */
    public Locator(Context context, Class<? extends Algorithm> algorithmClass) throws Exception {
        environment = Environment.getInstance(context);
        List<Ap> aps = environment.getAps();
        spaceInfo = SpaceInfo.getInstance(context);

        maps = spaceInfo.getAllMaps(aps);
        Map originalMap = spaceInfo.autoSelectMap(aps);
        map = copyMapBase(originalMap);
        mapIndex = maps.indexOf(originalMap);

        try {
            algorithm = algorithmClass.getConstructor(Map.class).newInstance(map);
        } catch (Exception e) {
            Log.e(TAG, "Locator: failed to instantiate the Algorithm: " + algorithmClass.getName(), e);
            throw e;
        }
    }

    private static Map copyMapBase(Map originalMap) {
        Map map = new Map();
        map.aps.addAll(originalMap.aps);
        map.height = originalMap.height;
        map.width = originalMap.width;
        map.name = originalMap.name;
        map.shapes = new HashSet<>(originalMap.shapes);
        return map;
    }

    /**
     * Get the maps available in the area.
     * @return A {@link List} of the available Maps.
     */
    public List<Map> getMaps() {
        return maps;
    }

    /**
     * Changes to another map.
     * @param index The index of the map in the {@link List} given by {@code getMaps()}.
     *
     */
    public void changeMap(int index) {
        mapIndex = index;
        map = copyMapBase(spaceInfo.selectMap(index));
    }

    /**
     * Get the index of the current map in the {@link List} given by {@code getMaps()}.
     * @return The index.
     */
    public int getMapIndex() {
        return mapIndex;
    }

    /**
     * Get the fingerprint at this position. Time-consuming.
     * @return The fingerprint.
     */
    public Fingerprint getFingerprint() {
        return environment.generateFingerprint(map.aps);
    }

    public Pair<Integer, Integer> locate(Fingerprint fingerprint) {
        return algorithm.locate(fingerprint);
    }

    public Pair<Integer, Integer> locate() {
        return locate(getFingerprint());
    }

    /**
     * Finish the work. The last map will be saved. All updated map will be stored to physical media.
     */
    public void finish() {
        environment.destroy();
        try {
            algorithm.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
