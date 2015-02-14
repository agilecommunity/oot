# --- !Ups

ALTER TABLE menu_item ADD discount_on_order decimal(10) not null default 0; -- --- 注文時の割引額
UPDATE menu_item SET discount_on_order = 0;

ALTER TABLE menu_item ADD fixed_on_order decimal(10) not null default 0; -- --- 注文時の定価
UPDATE menu_item set fixed_on_order = price_on_order;
ALTER TABLE menu_item DROP price_on_order;

# --- !Downs

ALTER TABLE menu_item DROP discount_on_order;
ALTER TABLE menu_item ADD price_on_order decimal(10) not null default 0; -- --- 注文時の定価
UPDATE menu_item set price_on_order = fixed_on_order;
ALTER TABLE menu_item DROP fixed_on_order;
