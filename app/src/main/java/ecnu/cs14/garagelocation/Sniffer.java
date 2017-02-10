package ecnu.cs14.garagelocation;

import android.content.Context;
import android.util.Pair;
import ecnu.cs14.garagelocation.data.Ap;
import ecnu.cs14.garagelocation.data.Fingerprint;
import ecnu.cs14.garagelocation.data.Map;
import ecnu.cs14.garagelocation.data.Sample;
import ecnu.cs14.garagelocation.env.Environment;
import ecnu.cs14.garagelocation.info.SpaceInfo;

import java.util.HashSet;
import java.util.List;

/**
 * Provides integrated and stateful use of the packages.
 * Created by K on 2017/1/29.
 */

public final class Sniffer {
    private Environment environment;
    private List<Map> maps;
    private int mapIndex;
    private Map map;
    private SpaceInfo spaceInfo;
    private boolean isChanged;

    /**
     * A time-consuming constructor.
     * @param context Context.
     */
    public Sniffer(Context context) {
        environment = Environment.getInstance(context);
        List<Ap> aps = environment.getAps();
        spaceInfo = SpaceInfo.getInstance(context);

        maps = spaceInfo.getAllMaps(aps);
        Map originalMap = spaceInfo.autoSelectMap(aps);
        map = copyMapBase(originalMap);
        mapIndex = maps.indexOf(originalMap);
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
     * @param storePrevious Whether previous chosen {@link Map} should be saved.
     */
    public void changeMap(int index, boolean storePrevious) {
        if (storePrevious) {
            save();
        }
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

    /**
     * Store the Sample given.
     * @param position A {@link Pair} of {@link Integer} indicating the position.
     * @param fingerprint The fingerprint.
     */
    public void storeSample(Pair<Integer, Integer> position, Fingerprint fingerprint) {
        map.samples.add(new Sample(position, fingerprint));
        isChanged = true;
    }

    public boolean needsSaving() {
        return isChanged;
    }

    public void save() {
        if (!needsSaving()) {
            return;
        }
        spaceInfo.updateMap(mapIndex, map);
        spaceInfo.saveAllMaps();
        isChanged = false;
    }

    /**
     * Finish the work. The last map will be saved. All updated map will be stored to physical media.
     */
    public void finish() {
        environment.destroy();
    }
}
