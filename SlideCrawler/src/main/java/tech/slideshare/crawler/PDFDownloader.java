package tech.slideshare.crawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class PDFDownloader {

    private static final Logger logger = LoggerFactory.getLogger(PDFDownloader.class);

    static Optional<Path> download(String url) throws IOException {
        try {
            Path temp = Files.createTempFile(null, null);
            temp.toFile().deleteOnExit();

            URLConnection connection = URI.create(url).toURL().openConnection();
            try (InputStream stream = connection.getInputStream()) {
                // リダイレクトしてログインページなどに飛んでしまうことがある。
                // その時はダウンロードせず、諦める。
                String contentType = connection.getHeaderField("Content-Type");
                if (!contentType.equals("application/pdf") && !contentType.equals("binary/octet-stream")) {
                    logger.warn("Ignored Content-Type: " + contentType + ", URL:" + connection.getURL());
                    return Optional.empty();
                }

                logger.debug("Download from " + connection.getURL());
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