package tech.slideshare.crawler;

import java.io.IOException;
import java.util.List;

public interface Crawler {
    String getURL();

    List<String> crawl(String url) throws IOException;
}
