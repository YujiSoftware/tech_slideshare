package tech.slideshare.collector.rss;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "item")
public class Item {
    @XmlElement(name = "title", namespace = "http://purl.org/rss/1.0/")
    public String title;

    @XmlElement(name = "link", namespace = "http://purl.org/rss/1.0/")
    public String link;

    @XmlElement(name = "description", namespace = "http://purl.org/rss/1.0/")
    public String description;

    @XmlElement(name = "date", namespace = "http://purl.org/dc/elements/1.1/")
    public String date;

    @XmlElement(name = "subject", namespace = "http://purl.org/dc/elements/1.1/")
    public String subject;
}
