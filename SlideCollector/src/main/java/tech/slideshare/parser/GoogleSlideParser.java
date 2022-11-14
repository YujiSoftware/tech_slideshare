package tech.slideshare.parser;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.slideshare.collector.Slide;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class GoogleSlideParser implements Parser {
    
    private static final Logger logger = LoggerFactory.getLogger(GoogleSlideParser.class);

    public static final int TIMEOUT = (int) TimeUnit.MILLISECONDS.toMinutes(1);

    public Optional<Slide> parse(String link) {
        try {
            Document doc = Jsoup.connect(link).timeout(TIMEOUT).get();

            // 権限が必要なページの場合、docs.google.com から accounts.google.com にリダイレクトされる。
            // その場合、プレゼンテーションは取得できないので処理をスキップする。
            URL location = new URL(doc.location());
            if (!location.getHost().equals("docs.google.com")) {
                return Optional.empty();
            }

            // /pub の場合、embedURL は取得できない
            if (!location.getPath().endsWith("/pub")) {
                // URL を正規化
                // (GoogleDocs の場合、embedURL から /preview のページを正とする)
                Optional<String> embed = doc.getElementsByTag("meta")
                        .stream()
                        .filter(e -> e.attr("itemprop").equals("embedURL"))
                        .findFirst()
                        .map(e -> e.attr("content"));

                // embedURL を取得できない場合、プレゼンテーションページではないと判断
                if (embed.isEmpty()) {
                    return Optional.empty();
                }

                if (!link.equals(embed.get())) {
                    link = embed.get();
                    doc = Jsoup.connect(link).timeout(TIMEOUT).get();
                }
            }

            // アンカーやクエリーを取り除いたURLを生成
            // (これらの有無によって、コンテンツは変わらないため)
            URL url = new URL(link);
            link = new URL(url.getProtocol(), url.getHost(), url.getPath()).toString();

            String title = doc.select("meta[property~=og:title]").attr("content");
            String author = null;
            String description = doc.select("meta[property~=og:description]").attr("content");
            String image = doc.select("meta[property~=og:image]").attr("content");

            return Optional.of(new Slide(title, link, author, null, description, image));
        } catch (HttpStatusException e) {
            logger.warn(String.format("Can't get GoogleSlide document. [url=%s, statusCode=%d]", e.getUrl(), e.getStatusCode()), e);
            return Optional.empty();
        } catch (IOException e) {
            logger.warn("Can't get GoogleSlide document.", e);
            return Optional.empty();
        }
    }
}
