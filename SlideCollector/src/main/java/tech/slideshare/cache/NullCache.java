package tech.slideshare.cache;

import java.time.Instant;

public class NullCache implements Cache {

    private final Instant updatedAt = Instant.now();

    public Instant updatedAt() {
        return updatedAt;
    }

    @Override
    public boolean add(String entry) {
        return true;
    }

    @Override
    public void flush() {
        // do nothing
    }
}
