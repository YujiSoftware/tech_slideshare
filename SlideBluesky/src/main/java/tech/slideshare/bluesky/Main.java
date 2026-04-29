package tech.slideshare.bluesky;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.slideshare.bluesky.database.SlideDao;
import tech.slideshare.bluesky.database.SlideDto;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static final int MAX_CHARACTER = 300;

    public static void main(String[] args) {
        String user = args[0];
        String password = args[1];

        String username = requireNonNull(System.getenv("BSKY_USERNAME"));
        String appPassword = requireNonNull(System.getenv("BSKY_APP_PASSWORD"));

        logger.info("Start {}", Main.class.getName());
        int exitCode = 0;

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/tech_slideshare", user, password)) {
            con.setAutoCommit(false);

            SlideDao slideDao = new SlideDao(con);
            SlideDto slide = slideDao.dequeue();
            if (slide != null) {
                try (Bluesky bluesky = new Bluesky(username, appPassword)) {
                    URI postedUrl = bluesky.createPost(makeText(slide), slide.getUrl());

                    logger.info("Post created. [id: {}, url: {}]", slide.getSlideId(), postedUrl);
                }
            }

            con.commit();
        } catch (Exception e) {
            logger.error("Failed to create post", e);
            exitCode = 1;
        }

        logger.info("End {}", Main.class.getName());
        System.exit(exitCode);
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