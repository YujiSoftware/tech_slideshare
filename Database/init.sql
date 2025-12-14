CREATE DATABASE tech_slideshare;
USE tech_slideshare;

CREATE TABLE slide (
  slide_id integer NOT NULL AUTO_INCREMENT,
  title text NOT NULL,
  url varchar(768) NOT NULL,
  author text DEFAULT NULL,
  twitter varchar(15) DEFAULT NULL,
  hash_tag text DEFAULT NULL,
  description text DEFAULT NULL,
  image varchar(2048) DEFAULT NULL,
  crawled_flag tinyint(1) NOT NULL DEFAULT 0,
  date timestamp NOT NULL DEFAULT current_timestamp(),
  updated_at timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (slide_id),
  UNIQUE KEY url (url)
);

CREATE TABLE tweet_queue (
  slide_id integer NOT NULL,
  PRIMARY KEY (slide_id),
  FOREIGN KEY (slide_id) REFERENCES slide (slide_id)
);

CREATE TABLE content (
  slide_id integer NOT NULL,
  page integer NOT NULL,
  content text NOT NULL,
  PRIMARY KEY (slide_id,page),
  FOREIGN KEY (slide_id) REFERENCES slide (slide_id)
);
