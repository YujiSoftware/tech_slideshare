package tech.slideshare.collector.rss;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "item")
public class Item {
    public String title;
    public String link;
    public String description;

    @XmlElement(name = "date", namespace = "http://purl.org/dc/elements/1.1/")
    public String date;
    @XmlElement(name = "subject", namespace = "http://purl.org/dc/elements/1.1/")
    public String subject;
}
