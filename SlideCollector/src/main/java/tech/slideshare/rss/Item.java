package tech.slideshare.rss;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "item")
public class Item {

    public static final DateTimeFormatter RSS_DATE_FORMATTER = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("Asia/Tokyo"));

    @XmlElement(name = "title", namespace = "http://purl.org/rss/1.0/")
    public String title;

    @XmlElement(name = "link", namespace = "http://purl.org/rss/1.0/")
    public String link;

    @XmlElement(name = "description", namespace = "http://purl.org/rss/1.0/")
    public String description;

    @XmlElement(name = "date", namespace = "http://purl.org/dc/elements/1.1/")
    public String date;

    @XmlElement(name = "subject", namespace = "http://purl.org/dc/elements/1.1/")
    public String[] subject;
}
