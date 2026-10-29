package hydrin.signedit;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class MineSkinFetcher {
    private static final String MINESKIN_URL = "https://api.mineskin.org/get/uuid/";

    public static String getValue(String id) {
        try {
            URL mineSkin = URI.create(MINESKIN_URL + id).toURL();
            URLConnection connection = mineSkin.openConnection();
            String response = new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            JsonObject json = JsonParser.parseString(response).getAsJsonObject();
            JsonObject data = json.getAsJsonObject("data");
            JsonObject texture = data.getAsJsonObject("texture");

            return texture.get("value").getAsString();
        } catch (IOException _) {
            return null;
        }
    }
}
