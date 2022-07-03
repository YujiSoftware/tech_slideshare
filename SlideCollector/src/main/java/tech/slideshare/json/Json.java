package tech.slideshare.json;

import java.util.List;

public class Json {
    public List<Item> items;

    public static class Item {
        public String title;
        public String author;
        public String twitter;
        public String link;
        public String date;
    }
}
