package tech.slideshare.rss;

import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HatenaBookmarkTest {
    @Test
    public void get() throws URISyntaxException, JAXBException, IOException {
        URL file = Objects.requireNonNull(getClass().getClassLoader().getResource("hatena.xml"));
        HatenaBookmark bookmark = new HatenaBookmark(file.toURI().toString());
        Item[] items = bookmark.get().toArray(Item[]::new);

        assertEquals(15, items.length);
        Item item = items[0];
        assertEquals("ホロラボカンファレンス 2022 基調講演 | ドクセル", item.title);
        assertEquals("https://www.docswell.com/s/HoloLab/K2JNVK-2022-04-28-133940?fbclid=IwAR3bQLEMYoUDRAGD2a3glzvfLI_6_XVnQb6r3lwmyzFDDNZycoWIYe-klqg", item.link);
        assertEquals("スライド概要 ホロラボカンファレンス 2022 基調講演のスライドです。 ホロラボカンファレンス 2022 イベントページ https://hololab.connpass.com/event/241174/ ホロラボカンファレンス 2022 タイムテーブル https://confengine.com/conferences/hololab-conference-2022/schedule/rich 各ページのテキスト 1. ホロラ...", item.description);
        assertEquals("2022-05-02T05:51:00Z", item.date);
        assertArrayEquals(new String[]{"テクノロジー"}, item.subject);
    }
}