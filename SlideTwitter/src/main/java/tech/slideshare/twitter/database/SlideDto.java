package tech.slideshare.twitter.database;

public class SlideDto {

    private int SlideId;
    private String title;
    private String url;

    public int getSlideId() {
        return SlideId;
    }

    public void setSlideId(int slideId) {
        SlideId = slideId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
