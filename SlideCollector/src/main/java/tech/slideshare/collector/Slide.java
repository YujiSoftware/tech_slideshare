package tech.slideshare.collector;

import java.time.ZonedDateTime;

public class Slide {
    private final String title;
    private final String link;
    private final ZonedDateTime date;
    private final String author;
    private final String twitter;

    private final String description;

    private final String image;

    public Slide(String title, String link, ZonedDateTime date, String author, String twitter, String description, String image) {
        this.title = title.replaceAll("\\p{Cntrl}", " ");
        this.link = link;
        this.date = date;
        this.author = author;
        this.twitter = twitter;
        this.description = description;
        this.image = image;
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

    public String getDescription() {
        return description;
    }

    public String getImage() {
        return image;
    }

    @Override
    public String toString() {
        return "Slide{" +
                "title='" + title + '\'' +
                ", link='" + link + '\'' +
                ", date=" + date +
                ", author='" + author + '\'' +
                ", twitter='" + twitter + '\'' +
                ", description='" + description + '\'' +
                ", image='" + image + '\'' +
                '}';
    }
}
