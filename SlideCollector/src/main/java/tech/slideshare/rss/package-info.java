@XmlSchema(xmlns = {
        @XmlNs(namespaceURI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#", prefix = "rdf"),
        @XmlNs(namespaceURI = "http://purl.org/rss/1.0/", prefix = ""),
        @XmlNs(namespaceURI = "http://purl.org/dc/elements/1.1/", prefix = "dc"),
})
package tech.slideshare.rss;

import jakarta.xml.bind.annotation.XmlNs;
import jakarta.xml.bind.annotation.XmlSchema;