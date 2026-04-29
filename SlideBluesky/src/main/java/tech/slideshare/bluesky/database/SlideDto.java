package tech.slideshare.bluesky.database;

import java.sql.Date;
import java.util.Objects;

public class SlideDto {
    private final int slideId;
    private final String title;
    private final String url;
    private final Date date;
    private final String author;
    private final String twitter;
    private final String hashTag;

    public SlideDto(int slideId, String title, String url, Date date,
                    String author, String twitter, String hashTag) {
        this.slideId = slideId;
        this.title = title;
        this.url = url;
        this.date = date;
        this.author = author;
        this.twitter = twitter;
        this.hashTag = hashTag;
    }

    public int getSlideId() {
        return slideId;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public Date getDate() {
        return date;
    }

    public String getAuthor() {
        return author;
    }

    public String getTwitter() {
        return twitter;
    }

    public String getHashTag() {
        return hashTag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SlideDto slideDto = (SlideDto) o;
        return slideId == slideDto.slideId &&
                Objects.equals(title, slideDto.title) &&
                Objects.equals(url, slideDto.url) &&
                Objects.equals(date, slideDto.date) &&
                Objects.equals(author, slideDto.author) &&
                Objects.equals(twitter, slideDto.twitter) &&
                Objects.equals(hashTag, slideDto.hashTag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(slideId, title, url, date, author, twitter, hashTag);
    }

    @Override
    public String toString() {
        return "SlideDto{" +
                "slideId=" + slideId +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", date=" + date +
                ", author='" + author + '\'' +
                ", twitter='" + twitter + '\'' +
                ", hashTag='" + hashTag + '\'' +
                '}';
    }
}

