package ecnu.cs14.garagelocation.info;

import android.os.Environment;
import ecnu.cs14.garagelocation.data.Ap;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.*;

/**
 * The low level of the map storing system. Loads JSONs for {@link MapSet}.
 * Created by K on 2017/1/26.
 */

final class FileSystem {
    private File path;
    private Map<Ap, String> index;
    FileSystem() {
        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        path = new File(path, "SnifferFingerprints");
        path.mkdirs();
    }

    private static String streamToString(InputStream stream) throws IOException {
        final int bufferSize = 1024;
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();
        Reader in = new InputStreamReader(stream);
        for (; ; ) {
            int readSize = in.read(buffer, 0, buffer.length);
            if (readSize < 0)
                break;
            out.append(buffer, 0, readSize);
        }
        return out.toString();
    }

    private static final String INDEX_FILENAME = "map-index.json";
    Map<Ap, String> getIndex() throws IOException, JSONException {
        if (index != null) {
            return index;
        }
        InputStream stream = new FileInputStream(new File(path, INDEX_FILENAME));
        String fileString = streamToString(stream);
        JSONObject json = new JSONObject(fileString);
        Iterator<String> keys = json.keys();
        index = new HashMap<>();
        while (keys.hasNext()) {
            String key = keys.next();
            index.put(new Ap("", key), json.getString(key));
        }
        return index;
    }

    private void updateIndex(List<Ap> aps, String filename) throws IOException, JSONException {
        Map<Ap, String> index = getIndex();
        for (Ap ap :
                aps) {
            index.put(ap, filename);
        }
        Map<String, String> stringIndex = new HashMap<>();
        for (Ap key :
                index.keySet()) {
            stringIndex.put(key.mac, filename);
        }
        JSONObject json = new JSONObject(stringIndex);
        OutputStream stream = new FileOutputStream(new File(path, INDEX_FILENAME));
        stream.write(json.toString().getBytes());
    }

    JSONObject getMapSetJson(String filename) throws IOException, JSONException {
        InputStream stream = new FileInputStream(new File(path, filename));
        String fileString = streamToString(stream);
        return new JSONObject(fileString);
    }

    JSONObject getMapSetJson(Ap ap) throws IOException, JSONException {
        if (null == index) {
            getIndex();
        }
        return getMapSetJson(index.get(ap));
    }

    void saveMapSet(MapSet mapSet) throws IOException, JSONException {
        Random random = new Random();
        String[] files = path.list();
        int filenameInt = random.nextInt();
        String filename;
        boolean continueFlag;
        do {
            continueFlag = false;
            filename = Integer.toString(filenameInt) + ".json";
            for (String file :
                    files) {
                if (file.equals(filename)) {
                    continueFlag = true;
                    filenameInt = random.nextInt();
                    break;
                }
            }
        } while (continueFlag);
        saveMapSet(filename, mapSet);
    }

    void saveMapSet(String filename, MapSet mapSet) throws IOException, JSONException {
        OutputStream stream = new FileOutputStream(new File(path, filename));
        stream.write(mapSet.toJson().toString().getBytes());
        updateIndex(mapSet.getAps(), filename);
    }
}
