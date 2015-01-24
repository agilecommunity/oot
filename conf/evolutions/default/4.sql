# --- !Ups

ALTER TABLE daily_menu ADD CONSTRAINT daily_menu_unique_menu_date UNIQUE(menu_date);

# --- !Downs

ALTER TABLE daily_menu DROP CONSTRAINT daily_menu_unique_menu_date;
