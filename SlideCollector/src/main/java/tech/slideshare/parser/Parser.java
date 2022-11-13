package tech.slideshare.parser;

import tech.slideshare.collector.Slide;

import java.time.ZonedDateTime;
import java.util.Optional;

public interface Parser {
    Optional<Slide> parse(String link, ZonedDateTime date);
}
