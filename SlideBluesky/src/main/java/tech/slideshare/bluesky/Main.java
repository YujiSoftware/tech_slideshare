package tech.slideshare.bluesky;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.slideshare.bluesky.database.SlideDto;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        // var user = args[0];
        // var password = args[1];

        String username = requireNonNull(System.getenv("BSKY_USERNAME"));
        String appPassword = requireNonNull(System.getenv("BSKY_APP_PASSWORD"));
        String postText = "Test";

//        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/tech_slideshare", user, password)) {
//            con.setAutoCommit(false);
//            SlideDao slideDao = new SlideDao(con);
//            slideDao.dequeue().ifPresent(slide -> {
//
//            });
//        } catch (SQLException ex) {
//            throw new RuntimeException(ex);
//        }

        try (Bluesky bluesky = new Bluesky(username, appPassword)) {
            URI postedUrl = bluesky.createPost(postText, "https://speakerdeck.com/loglassjoe/20260428-product-management-summit-loglass-joehirose");

            logger.info("Post created: {}", postedUrl);
        } catch (Exception e) {
            logger.error("Failed to create post", e);
            System.exit(1);
        }
    }

    static String makeText(SlideDto dto) {
        List<String> authors = new ArrayList<>();
        if (dto.getAuthor() != null) {
            authors.add(dto.getAuthor());
        }
        if (dto.getTwitter() != null) {
            authors.add("@" + dto.getTwitter());
        }

        String author = "";
        if (!authors.isEmpty()) {
            author = " (" + String.join(", ", authors) + ")";
        }

        String hashTag = "";
        if (dto.getHashTag() != null) {
            hashTag = " #" + dto.getHashTag();
        }

        String text = dto.getTitle();
        if (!author.isEmpty() || !hashTag.isEmpty()) {
            // 上限を超える場合、タイトルを省略する
            int remain = MAX_CHARACTER - length(author, hashTag, dto.getUrl(), "\n");
            if (length(text) > remain) {
                text = text.substring(0, remain - 1) + "…";
            }

            text += author + hashTag;
        }

        return text;
    }

    private static int length(String... strings) {
        return Arrays.stream(strings).mapToInt(s -> s.codePointCount(0, s.length())).sum();
    }
}