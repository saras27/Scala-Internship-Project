USE socialnetwork;

DROP TABLE IF EXISTS messages;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS likes;
DROP TABLE IF EXISTS friend_requests;
DROP TABLE IF EXISTS posts;
DROP TABLE IF EXISTS users;

CREATE TABLE users(
    user_id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(30) NOT NULL UNIQUE,
    password VARCHAR(200) NOT NULL,
    profile_picture VARCHAR(200),
    name VARCHAR(200),
    surname VARCHAR(200)
);

INSERT INTO users (username, password, name)
    VALUES ('sara', '123', 'sara');
INSERT INTO users (username, password)
    VALUES ('ana', '123');
INSERT INTO users (username, password)
    VALUES ('marko', '123');
INSERT INTO users (username, password, name, surname)
    VALUES ('andrej', '123', 'andrej', 'peric');


CREATE TABLE posts(
    post_id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
    user_id int NOT NULL,
    date_posted timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    content varchar(200)
);

INSERT INTO posts (user_id, content)
    VALUES (1, 'Hello everyone!');

CREATE TABLE likes(
    user_id int NOT NULL,
    post_id int NOT NULL,
    CONSTRAINT likes PRIMARY KEY (user_id, post_id)
);

INSERT INTO likes ( user_id, post_id)
    VALUES (2, 1);

CREATE TABLE friend_requests(
    request_id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
    user_sent_id int NOT NULL,
    user_got_id int NOT NULL,
    status ENUM('ACCEPTED', 'PENDING') DEFAULT 'PENDING'
);

INSERT INTO friend_requests (user_sent_id, user_got_id)
    VALUES (1, 3);

CREATE TABLE comments(
    comment_id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
    user_id int NOT NULL,
    post_id int NOT NULL,
    comment VARCHAR(200),
    date_posted timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (post_id) REFERENCES posts(post_id)
);

INSERT INTO comments(user_id, post_id, comment)
    VALUES (2, 1, 'This is a great post!');

CREATE TABLE messages(
    message_id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
    user_from int  NOT NULL,
    user_to int NOT NULL,
    message VARCHAR(200)
);

INSERT INTO messages(user_from, user_to, message)
    VALUES (1, 2, 'Hey, how are you?');