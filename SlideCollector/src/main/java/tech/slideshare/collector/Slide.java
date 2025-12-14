package tech.slideshare.collector;

public class Slide {
    private final String title;
    private final String link;
    private final String author;
    private final String twitter;

    private final String description;

    private final String image;

    private String hashTag;

    public Slide(String title, String link, String author, String twitter, String description, String image) {
        this.title = title.replaceAll("\\p{Cntrl}", " ");
        this.link = link;
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

    public String getHashTag() {
        return hashTag;
    }

    public void setHashTag(String hashTag) {
        this.hashTag = hashTag;
    }

    @Override
    public String toString() {
        return "Slide{" +
                "title='" + title + '\'' +
                ", link='" + link + '\'' +
                ", author='" + author + '\'' +
                ", twitter='" + twitter + '\'' +
                ", description='" + description + '\'' +
                ", image='" + image + '\'' +
                '}';
    }
}
