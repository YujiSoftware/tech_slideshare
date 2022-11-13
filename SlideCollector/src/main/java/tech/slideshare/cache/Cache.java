package tech.slideshare.cache;

import java.io.IOException;
import java.time.Instant;

public interface Cache {
    Instant updatedAt();

    boolean add(String entry);

    void flush() throws IOException;
}
