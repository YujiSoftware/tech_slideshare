CREATE DATABASE tech_slideshare;
USE tech_slideshare;

CREATE TABLE slide (
  slide_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  title    TEXT NOT NULL,
  url      VARCHAR(1024) NOT NULL UNIQUE,
  author   TEXT,
  twitter  VARCHAR(15),
  crawled_flag BOOLEAN NOT NULL DEFAULT FALSE,
  date     TIMESTAMP NOT NULL
);

CREATE TABLE tweet_queue (
  slide_id   INT NOT NULL PRIMARY KEY,
  FOREIGN KEY (slide_id) REFERENCES slide (slide_id)
);

CREATE TABLE content (
  slide_id INT NOT NULL,
  page     INT NOT NULL,
  content  TEXT NOT NULL,
  PRIMARY KEY(slide_id, page),
  FOREIGN KEY (slide_id) REFERENCES slide (slide_id)
);
