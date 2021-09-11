package tech.slideshare.rss;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "RDF", namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
public class Rss {
    @XmlElement(name = "channel", namespace = "http://purl.org/rss/1.0/")
    public Channel channel;

    @XmlElement(name = "item", namespace = "http://purl.org/rss/1.0/")
    public ArrayList<Item> items = new ArrayList<>();
}
