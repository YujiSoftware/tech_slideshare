CREATE DATABASE tech_slideshare;
USE tech_slideshare;

CREATE TABLE slide (
  slide_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  title    TEXT NOT NULL,
  url      VARCHAR(1024) NOT NULL UNIQUE,
  date     TIMESTAMP NOT NULL
);

CREATE TABLE tweet_queue (
  slide_id   INT NOT NULL PRIMARY KEY,
  FOREIGN KEY (slide_id) REFERENCES slide (slide_id)
);
