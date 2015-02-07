# --- !Ups

ALTER TABLE daily_menu ALTER COLUMN menu_date TYPE TIMESTAMP;
ALTER TABLE daily_order ALTER COLUMN order_date TYPE TIMESTAMP;

# --- !Downs

ALTER TABLE daily_menu ALTER COLUMN menu_date TYPE DATE;
ALTER TABLE daily_order ALTER COLUMN order_date TYPE DATE;
