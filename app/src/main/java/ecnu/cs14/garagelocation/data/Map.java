package ecnu.cs14.garagelocation.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Map and Shape data structures.
 * Created by K on 2017/1/22.
 */

public final class Map {
    public String name;
    public int width;
    public int height;
    public Set<Shape> shapes;
    public List<Ap> aps;
    public Set<Sample> samples;

    public static abstract class Shape {
        public enum Type {
            CIRCLE,
            RECT
        }
        public Type type;

        public static Shape fromJson(JSONObject json) {
            String typeString = json.optString("type");
            if ("circle".equals(typeString)) {
                Circle circle = new Circle();
                circle.center_left = json.optInt("center-left");
                circle.center_top = json.optInt("center-top");
                circle.radius = json.optInt("radius");
                return circle;
            } else if ("rect".equals(typeString)) {
                Rect rect = new Rect();
                rect.left = json.optInt("left");
                rect.top = json.optInt("top");
                rect.right = json.optInt("right");
                rect.bottom = json.optInt("bottom");
                return rect;
            }
            return null;
        }

        public abstract JSONObject toJson() throws JSONException;

        public static final class Circle extends Shape {
            public int center_left, center_top, radius;
            public Circle() {
                type = Type.CIRCLE;
            }
            @Override
            public JSONObject toJson() throws JSONException {
                return new JSONObject().put("type", "circle")
                        .put("center-left", center_left)
                        .put("center-top", center_top)
                        .put("radius", radius);
            }
        }

        public static final class Rect extends Shape {
            public int left, top, right, bottom;
            public Rect() {
                type = Type.RECT;
            }

            @Override
            public JSONObject toJson() throws JSONException {
                return new JSONObject().put("type", "rect")
                        .put("left", left)
                        .put("right", right)
                        .put("top", top)
                        .put("bottom", bottom);
            }
        }
    }

    public Map() {
        shapes = new HashSet<>();
        aps = new ArrayList<>();
        samples = new HashSet<>();
    }

    public Map(JSONObject json) throws JSONException {
        name = getNameFromJson(json);
        width = json.getInt("width");
        height = json.getInt("height");
        shapes = new HashSet<>();
        JSONArray shapesJson = json.optJSONArray("shapes");
        if (null != shapesJson) {
            for (int i = 0; i < shapesJson.length(); i++) {
                shapes.add(Shape.fromJson(shapesJson.getJSONObject(i)));
            }
        }
        aps = getApsFromJson(json);
        samples = new HashSet<>();
        JSONArray samplesJson = json.optJSONArray("samples");
        if (null != samplesJson) {
            for (int i = 0; i < samplesJson.length(); i++) {
                samples.add(new Sample(aps, samplesJson.getJSONObject(i)));
            }
        }
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", name).put("width", width).put("height", height);
        if (null != shapes) {
            for (Shape shape :
                    shapes) {
                json.accumulate("shapes", shape.toJson());
            }
        }
        for (Ap ap :
                aps) {
            json.accumulate("aps", ap.toJson());
        }
        for (Sample sample :
                samples) {
            json.accumulate("samples", sample.toJson(aps));
        }
        return json;
    }

    public static String getNameFromJson(JSONObject json) throws JSONException {
        return json.optString("name", "");
    }

    public static List<Ap> getApsFromJson(JSONObject json) throws JSONException{
        List<Ap> aps = new ArrayList<>();
        JSONArray apsJson = json.getJSONArray("aps");
        for (int i = 0; i < apsJson.length(); i++) {
            aps.add(new Ap(apsJson.getJSONObject(i)));
        }
        return aps;
    }
}
