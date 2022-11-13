package tech.slideshare.connpass;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConnpassTest {

    @Test
    void getEvents() throws IOException {
        List<Connpass.Event> events = Connpass.getEvents();

        assertEquals(1000, events.size());
    }
}