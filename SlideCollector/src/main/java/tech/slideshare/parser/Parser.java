package tech.slideshare.parser;

import tech.slideshare.collector.Slide;

import java.util.Optional;

public interface Parser {
    Optional<Slide> parse(String link);
}
