package ecnu.cs14.garagelocation.data;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Fingerprint data structure.
 * Created by K on 2017/1/23.
 */

public final class Fingerprint extends HashMap<Ap, List<Integer>> {
    public Fingerprint() {
        super();
    }

    public Fingerprint(List<Ap> base, JSONArray json) throws JSONException {
        super();
        for (int i = 0; i < base.size(); i++) {
            JSONArray signalsJson = json.getJSONArray(i);
            int length = signalsJson.length();
            ArrayList<Integer> signals = new ArrayList<>(length);
            for (int j = 0; j < length; j++) {
                signals.add(j, signalsJson.getInt(j));
            }
            put(base.get(i), signals);
        }
    }

    public JSONArray toJson(List<Ap> base) throws JSONException {
        JSONArray json = new JSONArray();
        for (int i = 0; i < base.size(); i++) {
//            json.put((int) get(base.get(i)));
            JSONArray signalsJson = new JSONArray(get(base.get(i)));
            json.put(signalsJson);
        }
        return json;
    }
}
