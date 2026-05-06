package tech.slideshare.bluesky.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class JsonObjectBuilder {
    private final JsonObject obj = new JsonObject();

    public JsonObjectBuilder add(String key, JsonObject value) {
        obj.add(key, value);
        return this;
    }

    public JsonObjectBuilder add(String key, JsonArray value) {
        obj.add(key, value);
        return this;
    }

    public JsonObjectBuilder add(String key, String value) {
        obj.addProperty(key, value);
        return this;
    }

    public JsonObjectBuilder add(String key, Number value) {
        obj.addProperty(key, value);
        return this;
    }

    public JsonObject build() {
        return obj;
    }
}