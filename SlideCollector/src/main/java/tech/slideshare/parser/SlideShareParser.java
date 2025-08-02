package tech.slideshare.parser;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.slideshare.collector.Slide;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Optional;

public class SlideShareParser implements Parser {

    private static final Logger logger = LoggerFactory.getLogger(SlideShareParser.class);

    public static final String USER_AGENT
            = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:140.0) Gecko/20100101 Firefox/140.0";

    private static final List<String> spamWords = List.of(
            "film",
            "Film",
            "-var4-",
            "4KTUBE-HD",
            "{{!VAR4}",
            "1877-546-7370"
    );

    public Optional<Slide> parse(String link) {
        try {
            if (link.contains("://www.slideshare.net/secret/")) {
                return Optional.empty();
            }

            Document doc;
            try {
                // SlideShare はデフォルトでモバイル用のページを返してくるので、
                // 明示的にPC用のユーザエージェントを設定する必要がある
                doc = Jsoup.connect(link).userAgent(USER_AGENT).get();
            } catch (HttpStatusException e) {
                // ファイルが削除された場合、410 GONE が返ってくる
                if (e.getStatusCode() == HttpURLConnection.HTTP_GONE) {
                    logger.debug("Deleted: {}", link);
                    return Optional.empty();
                }
                throw e;
            }

            // URL を正規化
            Optional<String> canonical = doc.getElementsByTag("link")
                    .stream()
                    .filter(e -> e.attr("rel").equals("canonical"))
                    .findFirst()
                    .map(e -> e.attr("href"));
            if (canonical.isPresent() && !link.equals(canonical.get())) {
                link = canonical.get();
                doc = Jsoup.connect(link).userAgent(USER_AGENT).get();
            }

            // スライドページかどうかの判定
            boolean isPresentation = doc.getElementsByTag("meta")
                    .stream()
                    .filter(e -> e.attr("name").equals("twitter:card"))
                    .anyMatch(e -> e.attr("content").equals("player"));
            if (!isPresentation) {
                logger.debug("Not presentation page: {}", link);
                return Optional.empty();
            }

            Element json = doc.getElementById("__NEXT_DATA__");
            if (json == null) {
                logger.debug("__NEXT_DATA__ not found: {}", link);
                return Optional.empty();
            }

            ObjectMapper mapper = new ObjectMapper();
            NextData nextData = mapper.readValue(json.html(), NextData.class);
            NextData.Props.PageProps.Slideshow slideshow = nextData.props.pageProps.slideshow;

            if (!slideshow.isViewable) {
                logger.debug("Not viewable: {}", link);
                return Optional.empty();
            }

            // 1ページしかないものは、スパムと判定して除外
            long pageCount = Integer.parseInt(slideshow.totalSlides);
            if (pageCount <= 5) {
                logger.debug("TotalSlides = 1: {}", link);
                return Optional.empty();
            }

            String title = slideshow.title;
            String author = slideshow.user.name;
            String twitter = getTwitter(slideshow.username);
            String description = slideshow.description;
            String image = doc.select("meta[property~=og:image]").attr("content");

            // スパム対策として、特定のキーワードを含むタイトルのものは除外
            for (String word : spamWords) {
                if (title.contains(word)) {
                    logger.debug("May be spam: {}, {}", title, link);
                    return Optional.empty();
                }
            }

            return Optional.of(new Slide(title, link, author, twitter, description, image));
        } catch (HttpStatusException e) {
            logger.warn(String.format("Can't get SlideShare document. [url=%s, statusCode=%d]", e.getUrl(), e.getStatusCode()), e);
            return Optional.empty();
        } catch (IOException e) {
            logger.warn("Can't get SlideShare document.", e);
            return Optional.empty();
        }
    }

    private static String getTwitter(String username) {
        try {
            String url = "https://www.slideshare.net/" + username;

            return Jsoup.connect(url).userAgent(USER_AGENT).get()
                    .select("a[aria-label='Twitter']")
                    .stream()
                    .findFirst()
                    .map(e -> e.attr("href"))
                    .map(t -> {
                        String[] paths = t.split("/");

                        return paths[paths.length - 1];
                    })
                    .orElse(null);
        } catch (HttpStatusException e) {
            logger.warn(String.format("Can't get SlideShare author. [url=%s, statusCode=%d]", e.getUrl(), e.getStatusCode()), e);
            return null;
        } catch (IOException e) {
            logger.warn("Can't get SlideShare author.", e);
            return null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class NextData {
        /*
        {
          "props": {
            "pageProps": {
              "slideshow": {
                "username": "simizu706",
                "canonicalUrl": "https://www.slideshare.net/simizu706/cmdstanrreducesum",
                "createdAt": "2021-09-13 07:59:29 UTC",
                "description": "Cmdstanrとreduce_sum()の使い方を解説します",
                "isPrivate": false,
                "isViewable": true,
                "thumbnail": "https://cdn.slidesharecdn.com/ss_thumbnails/cmdstanrintroduction-210913075930-thumbnail.jpg?width=640\\u0026height=640\\u0026fit=bounds",
                "title": "Cmdstanr入門とreduce_sum()解説",
                "totalSlides": 55,
                "type": "presentation",
                "user": {
                  "id": "51751916",
                  "login": "simizu706",
                  "name": "Hiroshi Shimizu"
                }
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
                    public String username;
                    public String canonicalUrl;
                    public String description;
                    public boolean isPrivate;
                    public boolean isViewable;
                    public String title;
                    public String totalSlides;
                    public User user;

                    @JsonIgnoreProperties(ignoreUnknown = true)
                    public static class User {
                        public String id;
                        public String login;
                        public String name;
                    }
                }
            }
        }
    }
}
