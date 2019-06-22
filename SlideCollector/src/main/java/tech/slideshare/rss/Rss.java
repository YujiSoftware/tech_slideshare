package tech.slideshare.rss;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "RDF", namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
public class Rss {
    @XmlElement(name = "channel", namespace = "http://purl.org/rss/1.0/")
    public Channel channel;

    @XmlElement(name = "item", namespace = "http://purl.org/rss/1.0/")
    public ArrayList<Item> items = new ArrayList<>();
}
