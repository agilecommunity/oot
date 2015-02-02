# --- !Ups

ALTER TABLE daily_menu ALTER COLUMN menu_date TIMESTAMP;
ALTER TABLE daily_order ALTER COLUMN order_date TIMESTAMP;

# --- !Downs

ALTER TABLE daily_menu ALTER COLUMN menu_date DATE;
ALTER TABLE daily_order ALTER COLUMN order_date DATE;
