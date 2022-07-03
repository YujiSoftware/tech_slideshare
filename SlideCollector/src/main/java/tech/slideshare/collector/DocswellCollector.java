package tech.slideshare.collector;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.xml.bind.JAXBException;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.slideshare.rss.Bookmark;
import tech.slideshare.rss.HatenaBookmark;
import tech.slideshare.rss.Item;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class DocswellCollector implements SlideCollector {

    private static final Logger logger = LoggerFactory.getLogger(DocswellCollector.class);

    private static final Pattern TWITTER = Pattern.compile("https://twitter.com/([^/]+)");

    private final Bookmark bookmark;

    public DocswellCollector() {
        bookmark = new HatenaBookmark("https://b.hatena.ne.jp/site/www.docswell.com/?mode=rss");
    }

    public DocswellCollector(Bookmark bookmark) {
        this.bookmark = bookmark;
    }

    @Override
    public Stream<Slide> collect() throws JAXBException, IOException {
        return bookmark.get()
                .filter(i -> i.link.startsWith("https://www.docswell.com/s/"))
                .map(i -> getSlide(i).orElse(null))
                .filter(Objects::nonNull);
    }

    private static Optional<Slide> getSlide(Item item) {
        try {
            String link = item.link;

            Document doc = Jsoup.connect(link).get();
            Element json = doc.getElementsByTag("script").select("[type=application/ld+json]").first();

            ObjectMapper mapper = new ObjectMapper();
            Article article = mapper.readValue(json.data(), Article.class);

            if (!article.type.equals("Article")) {
                return Optional.empty();
            }

            String title = article.headline;
            String author = article.author.name;
            String twitter = getTwitter(article.author.url);
            String description = article.description;
            String image = article.image;

            return Optional.of(new Slide(title, link, item.date, author, twitter, description, image));
        } catch (HttpStatusException e) {
            logger.warn(String.format("Can't get Docswell document. [url=%s, statusCode=%d]", e.getUrl(), e.getStatusCode()), e);
            return Optional.empty();
        } catch (IOException e) {
            logger.warn("Can't get Docswell document.", e);
            return Optional.empty();
        }
    }

    private static String getTwitter(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            Element json = doc.getElementsByTag("script").select("[type=application/ld+json]").first();

            ObjectMapper mapper = new ObjectMapper();
            Person person = mapper.readValue(json.data(), Person.class);

            for (String as : person.sameAs) {
                Matcher matcher = TWITTER.matcher(as);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }

            return null;
        } catch (HttpStatusException e) {
            logger.warn(String.format("Can't get Docswell author. [url=%s, statusCode=%d]", e.getUrl(), e.getStatusCode()), e);
            return null;
        } catch (IOException e) {
            logger.warn("Can't get Docswell author.", e);
            return null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Article {
        /*
        {
          "@context": "https://schema.org",
          "@type": "Article",
          "mainEntityOfPage": {
            "@type": "WebPage",
            "@id": "https://www.docswell.com/s/kojimadev/534M2Z-2022-04-27-002038"
          },
          "headline": "主体性を失わせるアンチパターンをやってしまっていた話",
          "description": "Engineering Manager Meetup #10 \u3067\u306e\u767a\u8868\u8cc7\u6599\u3067\u3059\u3002\r\nhttps:\/\/engineering-manager-meetup.connpass.com\/event\/239203\/",
          "image": "https://www.docswell.com/thumbnail/NPJX1D85JX.jpg",
          "author": {
            "@type": "Person",
            "name": "小島 優介",
            "url": "https://www.docswell.com/user/kojimadev"
          },
          "publisher": {
            "@type": "Organization",
            "name": "ドクセル",
            "logo": {
              "@type": "ImageObject",
              "url": "https://www.docswell.com/assets/images/logo_square.png"
            }
          },
          "datePublished": "2022-04-27",
          "dateModified": "2022-04-27"
        }
         */
        @JsonProperty("@context")
        public String context;

        @JsonProperty("@type")
        public String type;

        public MainEntityOfPage mainEntityOfPage;

        public String headline;
        public String description;
        public String image;
        public Author author;

        public static class MainEntityOfPage {
            @JsonProperty("@type")
            public String type;

            @JsonProperty("@id")
            public String id;
        }

        public static class Author {
            @JsonProperty("@type")
            public String type;

            public String name;
            public String url;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Person {
        /*
        {
          "@context": "https://schema.org/",
          "@type": "Person",
          "name": "小島 優介",
          "url": "https://www.docswell.com/user/kojimadev",
          "image": "https://www.docswell.com/icon/6XJ2Q9P2.jpg",
          "sameAs": [
                    "https://twitter.com/kojimadev", "https://github.com/kojimadev", "https://kojimadev.anotion.so/"
          ]
        }
        */
        @JsonProperty("@context")
        public String context;

        @JsonProperty("@type")
        public String type;

        public String name;
        public String url;
        public String image;
        public String[] sameAs;
    }
}
