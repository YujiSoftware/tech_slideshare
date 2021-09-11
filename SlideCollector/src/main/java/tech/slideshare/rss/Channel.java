package tech.slideshare.rss;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "channel")
public class Channel {
    @XmlElement(name = "title", namespace = "http://purl.org/rss/1.0/")
    public String title;

    @XmlElement(name = "link", namespace = "http://purl.org/rss/1.0/")
    public String link;

    @XmlElement(name = "description", namespace = "http://purl.org/rss/1.0/")
    public String description;
}
