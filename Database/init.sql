CREATE DATABASE tech_slideshare;
USE tech_slideshare;

CREATE TABLE slide (
  slide_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  title  TEXT NOT NULL,
  url    TEXT NOT NULL
);

CREATE TABLE tweet_queue (
  slide_id   INT NOT NULL PRIMARY KEY,
  date     TIMESTAMP NOT NULL,
  FOREIGN KEY (slide_id) REFERENCES slide (slide_id)
);
