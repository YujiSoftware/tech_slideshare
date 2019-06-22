package tech.slideshare.collector;

import tech.slideshare.rss.Item;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Supplier;

public class Slide {
    private final String title;
    private final String link;
    private final ZonedDateTime date;
    private final Supplier<Optional<String>> author;

    public Slide(Item item, Supplier<Optional<String>> author) {
        this.title = item.title;
        this.link = item.link;
        this.date = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("Asia/Tokyo")).parse(item.date, ZonedDateTime::from);
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public ZonedDateTime getDate() {
        return date;
    }

    public Optional<String> getAuthor() {
        return author.get();
    }
}
