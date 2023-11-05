package tech.slideshare.crawler;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SlideShareCrawler implements Crawler {

    public static final String USER_AGENT
            = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:119.0) Gecko/20100101 Firefox/119.0";

    @Override
    public String getURL() {
        return "https://www.slideshare.net/";
    }

    public List<String> crawl(String url) throws IOException {
        List<String> contents = new ArrayList<>();

        Document doc = Jsoup.connect(url).userAgent(USER_AGENT).get();

        Element json = doc.getElementById("__NEXT_DATA__");
        if (json == null) {
            return contents;
        }

        ObjectMapper mapper = new ObjectMapper();
        NextData nextData = mapper.readValue(json.html(), NextData.class);

        for (String html : nextData.props.pageProps.slideshow.transcript.html) {
            contents.add(Parser.unescapeEntities(html, true).trim());
        }

        return contents;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class NextData {
        /*
        {
          "props": {
            "pageProps": {
              "slideshow": {
                "title": "JEP280: Java 9 で文字列結合の処理が変わるぞ！準備はいいか！？ #jjug_ccc",
                "totalSlides": 23,
                "transcript": {
                  "html": [
                    "Java 9 \u0026#x3067;\n\u0026#x6587;\u0026#x5B57;\u0026#x5217;\u0026#x7D50;\u0026#x5408;\u0026#x306E;\n\u0026#x51E6;\u0026#x7406;\u0026#x304C;\u0026#x5909;\u0026#x308F;\u0026#x308B;\u0026#x305E;\u0026#xFF01;\n\u0026#x6E96;\u0026#x5099;\u0026#x306F;\u0026#x3044;\u0026#x3044;\u0026#x304B;\u0026#xFF01;\u0026#xFF1F;\n@YujiSoftware\n ",
                  ]
                },
                "type": "presentation"
              }
            }
          }
        }
         */
        public Props props;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Props {
            public PageProps pageProps;

            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class PageProps {
                public Slideshow slideshow;

                @JsonIgnoreProperties(ignoreUnknown = true)
                public static class Slideshow {
                    public Transcript transcript;

                    @JsonIgnoreProperties(ignoreUnknown = true)
                    public static class Transcript {
                        public String[] html;
                    }
                }
            }
        }
    }
}
