package tech.slideshare.bluesky;

import com.google.gson.JsonObject;
import org.apache.hc.core5.http.HttpEntity;

import java.io.IOException;

public interface BlueskyClient extends AutoCloseable {
    String getDid();

    JsonObject post(String endpoint, HttpEntity entity) throws IOException;
}
