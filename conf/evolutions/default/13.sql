# --- !Ups

ALTER TABLE menu_item ADD register_number varchar(20) not null default '';          -- レジ番号
ALTER TABLE menu_item ADD item_number varchar(20) not null default '';              -- 商品番号
ALTER TABLE menu_item ADD fixed_on_purchase_exc_tax decimal(10) not null default 0; -- 発注時の価格(税抜)
ALTER TABLE menu_item ADD fixed_on_purchase_inc_tax decimal(10) not null default 0; -- 発注時の価格(税込)

# --- !Downs

ALTER TABLE menu_item DROP register_number;
ALTER TABLE menu_item DROP item_number;
ALTER TABLE menu_item DROP fixed_on_purchase_exc_tax;
ALTER TABLE menu_item DROP fixed_on_purchase_inc_tax;
