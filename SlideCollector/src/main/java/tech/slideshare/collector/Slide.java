package tech.slideshare.collector;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Slide {
    private final String title;
    private final String link;
    private final ZonedDateTime date;
    private final String author;
    private final String twitter;

    public Slide(String title, String link, String date, String author, String twitter) {
        this.title = title;
        this.link = link;
        this.date = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("Asia/Tokyo")).parse(date, ZonedDateTime::from);
        this.author = author;
        this.twitter = twitter;
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

    public String getAuthor() {
        return author;
    }

    public String getTwitter() {
        return twitter;
    }
}
