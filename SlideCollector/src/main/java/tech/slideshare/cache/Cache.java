package tech.slideshare.cache;

import java.io.IOException;

public interface Cache {
    boolean add(String entry);

    void flush() throws IOException;
}
