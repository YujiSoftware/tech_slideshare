package tech.slideshare.bluesky;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * URLからOGP（Open Graph Protocol）情報を取得するクラス
 */
public class OGP {
    private static final Logger logger = LoggerFactory.getLogger(OGP.class);

    private static final CloseableHttpClient httpClient = HttpClients.createDefault();

    /**
     * URLからOGP情報を取得
     *
     * @param url 対象URL
     * @return OGP情報（タイトル、説明、画像URLなど）
     * @throws IOException 取得失敗時
     */
    public static OGPInfo extract(String url) throws IOException {
        HttpGet request = new HttpGet(url);
        request.setHeader("User-Agent", "Mozilla/5.0 (compatible; BlueskyBot/1.0)");

        return httpClient.execute(request, response -> {
            if (response.getCode() != 200) {
                throw new IOException("Failed to fetch URL: " + response.getCode());
            }

            String html = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            return parseOGP(html, url);
        });
    }

    /**
     * HTMLからOGPメタタグをパース
     *
     * @param html HTMLコンテンツ
     * @param url  元のURL
     * @return OGP情報
     */
    private static OGPInfo parseOGP(String html, String url) {
        Document doc = Jsoup.parse(html);
        Map<String, String> ogp = new HashMap<>();

        // OGPメタタグを取得
        for (Element meta : doc.select("meta[property^=og:]")) {
            String property = meta.attr("property");
            String content = meta.attr("content");
            if (!content.isEmpty()) {
                ogp.put(property, content);
            }
        }

        // Twitter Cardも取得（フォールバック用）
        for (Element meta : doc.select("meta[name^=twitter:]")) {
            String name = meta.attr("name");
            String content = meta.attr("content");
            if (!content.isEmpty()) {
                ogp.put(name, content);
            }
        }

        // タイトルを取得（OGP → Twitter Card → HTML titleの順）
        String title = ogp.get("og:title");
        if (title == null || title.isEmpty()) {
            title = ogp.get("twitter:title");
        }
        if (title == null || title.isEmpty()) {
            title = doc.title();
        }
        if (title == null || title.isEmpty()) {
            title = url; // 最終フォールバック
        }

        // 説明を取得
        String description = ogp.get("og:description");
        if (description == null || description.isEmpty()) {
            description = ogp.get("twitter:description");
        }
        if (description == null || description.isEmpty()) {
            description = ""; // 最終フォールバック
        }

        // 画像URLを取得
        String imageUrl = ogp.get("og:image");
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageUrl = ogp.get("twitter:image");
        }

        // 相対URLを絶対URLに変換
        if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.startsWith("http")) {
            try {
                java.net.URI baseUri = java.net.URI.create(url);
                java.net.URI resolvedUri = baseUri.resolve(imageUrl);
                imageUrl = resolvedUri.toString();
            } catch (Exception e) {
                logger.warn("Failed to resolve image URL: {}", imageUrl);
                imageUrl = null;
            }
        }

        return new OGPInfo(title, description, imageUrl, url);
    }

    /**
     * OGP情報を格納するクラス
     */
    public record OGPInfo(String title, String description, String imageUrl, String url) {
    }
}
