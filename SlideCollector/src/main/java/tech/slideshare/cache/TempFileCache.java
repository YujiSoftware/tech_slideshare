package tech.slideshare.cache;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TempFileCache implements Cache {

    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

    private static final Path CACHE_DIR = Path.of(TEMP_DIR, "SlideCollector");

    private final Instant updatedAt;

    private final Set<String> cache;

    private final Path file;

    public TempFileCache(String name) throws IOException {
        Files.createDirectories(CACHE_DIR);
        this.file = CACHE_DIR.resolve(name + ".log");

        if (Files.exists(this.file)) {
            List<String> list = Files.readAllLines(this.file);
            this.updatedAt = Instant.parse(list.getFirst());
            this.cache = new HashSet<>(list.subList(1, list.size()));
        } else {
            this.updatedAt = Instant.now();
            this.cache = new HashSet<>();
        }
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public boolean add(String entry) {
        return this.cache.add(entry);
    }

    public void flush() throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(this.file)) {
            writer.write(Instant.now().toString());
            writer.newLine();

            for (String entry : this.cache) {
                writer.write(entry);
                writer.newLine();
            }
        }
    }
}
