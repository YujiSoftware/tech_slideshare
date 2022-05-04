package tech.slideshare.rss;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.slideshare.common.DigestUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HatenaBookmark implements Bookmark {

    private static final Logger logger = LoggerFactory.getLogger(HatenaBookmark.class);

    private final String url;

    public HatenaBookmark(String url) {
        this.url = url;
    }

    public Stream<Item> get() throws JAXBException, IOException {
        JAXBContext context = JAXBContext.newInstance(Rss.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();

        Rss r = (Rss) unmarshaller.unmarshal(new URL(url));

        Set<String> used = cache(r.items);
        return r.items
                .stream()
                .filter(i -> !used.contains(i.link))
                .filter(i -> i.subject != null && Arrays.asList(i.subject).contains("テクノロジー"));
    }

    public Set<String> cache(List<Item> items) throws IOException {
        String tempDir = System.getProperty("java.io.tmpdir");
        Path dir = Path.of(tempDir, "SlideCollector");
        Files.createDirectories(dir);

        Path file = dir.resolve(DigestUtils.getSHA1(url) + ".log");
        logger.info(String.format("Cached. [url=%s, file=%s, count=%d]", url, file, items.size()));

        // 前回の内容を取得
        List<String> lines = Files.exists(file) ? Files.readAllLines(file) : Collections.emptyList();

        // 今回の内容を保存
        Files.writeString(file, items.stream().map(i -> i.link).collect(Collectors.joining(System.lineSeparator())));

        return new HashSet<>(lines);
    }
}
