package tech.slideshare.rss;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

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
