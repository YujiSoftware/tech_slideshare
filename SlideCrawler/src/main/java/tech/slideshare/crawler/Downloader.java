package tech.slideshare.crawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class Downloader {

    private static final Logger logger = LoggerFactory.getLogger(Downloader.class);

    static Optional<Path> download(String url) throws IOException {
        try {
            Path temp = Files.createTempFile(null, null);
            temp.toFile().deleteOnExit();
            try (InputStream stream = URI.create(url).toURL().openStream()) {
                Files.copy(stream, temp, StandardCopyOption.REPLACE_EXISTING);
            }

            return Optional.of(temp);
        } catch (FileNotFoundException e) {
            // 404 NotFound, 410 Gone の場合、FileNotFoundException
            logger.warn("FileNotFound: " + url);
            return Optional.empty();
        } catch (IOException e) {
            if (e.getMessage().contains("Server returned HTTP response code: 401")) {
                logger.warn(e.getMessage());
                return Optional.empty();
            }
            throw e;
        }
    }
}