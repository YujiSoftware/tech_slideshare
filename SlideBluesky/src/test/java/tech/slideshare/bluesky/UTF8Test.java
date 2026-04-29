package tech.slideshare.bluesky;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Source - https://stackoverflow.com/a/8512877
// Posted by McDowell, modified by community. See post 'Timeline' for change history
// Retrieved 2026-04-29, License - CC BY-SA 3.0
class UTF8Test {
    @Test
    public void testUtf8Len() {
        AllCodepointsIterator iterator = new AllCodepointsIterator();
        while (iterator.hasNext()) {
            String test = new String(Character.toChars(iterator.next()));
            assertEquals(test.getBytes(StandardCharsets.UTF_8).length, UTF8.length(test));
        }
    }

    private static class AllCodepointsIterator {
        private static final int MAX = 0x10FFFF; //see http://unicode.org/glossary/
        private static final int SURROGATE_FIRST = 0xD800;
        private static final int SURROGATE_LAST = 0xDFFF;
        private int codepoint = 0;

        public boolean hasNext() {
            return codepoint < MAX;
        }

        public int next() {
            int ret = codepoint;
            codepoint = next(codepoint);
            return ret;
        }

        private int next(int codepoint) {
            while (codepoint++ < MAX) {
                if (codepoint == SURROGATE_FIRST) {
                    codepoint = SURROGATE_LAST + 1;
                }
                if (!Character.isDefined(codepoint)) {
                    continue;
                }
                return codepoint;
            }
            return MAX;
        }
    }
}