package tech.slideshare.connpass;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConpassTest {

    private static final ZoneId UTC = ZoneId.of("UTC");

    @Test
    public void mapper() throws IOException {
        URL url = new URL("https://connpass.com/api/v1/event/?event_id=364");

        Connpass connpass = Connpass.mapper.readValue(url, Connpass.class);

        assertEquals(1, connpass.resultsReturned());
        assertEquals(1, connpass.resultsAvailable());
        assertEquals(1, connpass.resultsReturned());

        assertEquals(1, connpass.events().size());
        Connpass.Event event = connpass.events().get(0);
        assertEquals(364, event.id());
        assertEquals("BPStudy#56", event.title());
        assertEquals("株式会社ビープラウドが主催するWeb系技術討論の会", event.catchText());
        assertEquals("<div style=\"font-size:10pt;font-style:normal;font-weight:normal;\"><div style=\"font-size:10pt;\"><span style=\"font-size:10pt;\">BPStudy#56はオープンクラウドキャンパスさん、hbstudyさんとの共同開催です。</span></div></div><div style=\"font-size:10pt;font-style:normal;font-weight:normal;\"><br /></div><div style=\"font-size:10pt;\"><font size=\"4\" color=\"#ff6666\"><span>「</span><font>USクラウド最新動向勉強会 Softlayer社に学ぶ競争力」</font></font></div><div style=\"font-size:10pt;\"><font size=\"2\"><br /></font></div><div style=\"font-style:normal;font-weight:normal;\"><font size=\"4\">参加登録は以下からお願いします。</font></div><div style=\"font-size:10pt;font-style:normal;font-weight:normal;\">↓　↓　↓</div><div><font size=\"4\"><a href=\"http://connpass.com/event/360/\" rel=\"nofollow\">http://connpass.com/event/360/</a></font></div>", event.description());
        assertEquals("https://bpstudy.connpass.com/event/364/", event.url());
        assertEquals("bpstudy", event.hashTag());
        assertEquals(ZonedDateTime.of(2012, 4, 17, 9, 30, 0, 0, UTC), event.startedAt());
        assertEquals(ZonedDateTime.of(2012, 4, 17, 11, 30, 0, 0, UTC), event.endedAt());
        assertEquals(0, event.limit());
        assertEquals("participation", event.eventType());
        assertEquals("東京都港区北青山2-8-44", event.address());
        assertEquals("先端技術館＠TEPIA", event.place());
        assertEquals(35.672968000000, event.lat());
        assertEquals(139.716904600000, event.lon());
        assertEquals(8, event.ownerId());
        assertEquals("haru860", event.ownerNickname());
        assertEquals("佐藤 治夫", event.ownerDisplayName());
        assertEquals(0, event.accepted());
        assertEquals(0, event.waiting());
        assertEquals(ZonedDateTime.of(2014, 6, 30, 1, 6, 19, 0, UTC), event.updatedAt());

        Connpass.Event.Series series = event.series();
        assertEquals(1, series.id());
        assertEquals("BPStudy", series.title());
        assertEquals("https://bpstudy.connpass.com/", series.url());
    }
}
