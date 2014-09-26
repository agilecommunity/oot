# --- !Ups

ALTER TABLE menu_item ADD code varchar(255);
UPDATE menu_item SET code = '';

# --- !Downs

ALTER TABLE menu_item DROP code;
