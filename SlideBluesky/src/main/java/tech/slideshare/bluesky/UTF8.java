package tech.slideshare.bluesky;

public class UTF8 {
    public static int length(String text) {
        int count = 0;
        for (int i = 0, len = text.length(); i < len; i++) {
            char c = text.charAt(i);
            if (c <= 0x007F) {
                count += 1;
            } else if (c <= 0x07FF) {
                count += 2;
            } else if (Character.isHighSurrogate(c)) {
                count += 4;
                i++; // Skip low surrogate
            } else {
                count += 3;
            }
        }

        return count;
    }
}
