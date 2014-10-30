# --- !Ups

ALTER TABLE menu_item ADD comment varchar(255);
UPDATE menu_item SET comment = '';

# --- !Downs

ALTER TABLE menu_item DROP comment;
