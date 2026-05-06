package tech.slideshare.bluesky.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class JsonArrayBuilder {
    private final JsonArray array = new JsonArray();

    public JsonArrayBuilder add(JsonObject value) {
        array.add(value);
        return this;
    }

    public JsonArrayBuilder add(JsonArray value) {
        array.add(value);
        return this;
    }

    public JsonArrayBuilder add(String value) {
        array.add(value);
        return this;
    }

    public JsonArrayBuilder add(Number value) {
        array.add(value);
        return this;
    }

    public JsonArray build() {
        return array;
    }
}