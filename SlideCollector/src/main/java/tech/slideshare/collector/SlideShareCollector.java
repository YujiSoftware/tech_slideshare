package tech.slideshare.collector;

public class SlideShareCollector extends HatenaBookmarkCollector {

    public SlideShareCollector() {
        super("http://b.hatena.ne.jp/entrylist?url=http%3A%2F%2Fwww.slideshare.net%2F&mode=rss");
    }
}
