package tech.slideshare.collector;

public class DocsCollector extends HatenaBookmarkCollector {

    public DocsCollector() {
        super("http://b.hatena.ne.jp/entrylist?url=http%3A%2F%2Fdocs.com%2F&mode=rss");
    }
}
