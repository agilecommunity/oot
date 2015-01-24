# --- !Ups

ALTER TABLE menu_item ADD status varchar(20); -- --- ステータス (有効:valid、無効:stopped、休眠:suspended)
UPDATE menu_item SET status = 'valid';

# --- !Downs

ALTER TABLE menu_item DROP status;
