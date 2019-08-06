package haveric.stackableItems.uuidFetcher;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.Callable;

import org.bukkit.Bukkit;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.common.collect.ImmutableList;

public class UUIDFetcher implements Callable<Map<String, UUID>> {
    private static final double PROFILES_PER_REQUEST = 100;
    private static final String PROFILE_URL = "https://api.mojang.com/profiles/minecraft";
    private final JSONParser jsonParser = new JSONParser();
    private final List<String> names;
    private final boolean rateLimiting;

    private static HashMap<String, UUID> lookupCache;

    public static void addPlayerToCache(String name, UUID uuid) {
        if (Bukkit.getServer().getOnlineMode()) {
            if (lookupCache == null) {
                lookupCache = new HashMap<String, UUID>();
            }

            lookupCache.put(name, uuid);
        }
    }

    public static void removePlayerFromCache(String name) {
        if (lookupCache != null) {
            if (lookupCache.containsKey(name)) {
                lookupCache.remove(name);
            }
        }
    }

    public UUIDFetcher(List<String> newNames, boolean newRateLimiting) {
        names = ImmutableList.copyOf(newNames);
        rateLimiting = newRateLimiting;
    }

    public UUIDFetcher(List<String> newNames) {
        this(newNames, true);
    }

    public Map<String, UUID> call() throws Exception {
        if (lookupCache == null) {
            lookupCache = new HashMap<String, UUID>();
        }

        Map<String, UUID> uuidMap = new HashMap<String, UUID>();

        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);

            if (lookupCache.containsKey(name)) {
                uuidMap.put(name, lookupCache.get(name));
                names.remove(i);
                i--;
            }
        }

        int requests = (int) Math.ceil(names.size() / PROFILES_PER_REQUEST);
        for (int i = 0; i < requests; i++) {
            HttpURLConnection connection = createConnection();
            String body = JSONArray.toJSONString(names.subList(i * 100, Math.min((i + 1) * 100, names.size())));
            writeBody(connection, body);
            JSONArray array = (JSONArray) jsonParser.parse(new InputStreamReader(connection.getInputStream()));
            for (Object profile : array) {
                JSONObject jsonProfile = (JSONObject) profile;
                String id = (String) jsonProfile.get("id");
                String name = (String) jsonProfile.get("name");
                UUID uuid = UUIDFetcher.getUUID(id);
                uuidMap.put(name, uuid);
                lookupCache.put(name, uuid);
            }
            if (rateLimiting && i != requests - 1) {
                Thread.sleep(100L);
            }
        }

        return uuidMap;
    }

    private static void writeBody(HttpURLConnection connection, String body) throws Exception {
        OutputStream stream = connection.getOutputStream();
        stream.write(body.getBytes());
        stream.flush();
        stream.close();
    }

    private static HttpURLConnection createConnection() throws Exception {
        URL url = new URL(PROFILE_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        return connection;
    }

    private static UUID getUUID(String id) {
        return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20, 32));
    }

    public static byte[] toBytes(UUID uuid) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        return byteBuffer.array();
    }

    public static UUID fromBytes(byte[] array) {
        if (array.length != 16) {
            throw new IllegalArgumentException("Illegal byte array length: " + array.length);
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(array);
        long mostSignificant = byteBuffer.getLong();
        long leastSignificant = byteBuffer.getLong();
        return new UUID(mostSignificant, leastSignificant);
    }

    public static UUID getUUIDOf(String name) throws Exception {
        return new UUIDFetcher(Collections.singletonList(name)).call().get(name);
    }
}