# --- !Ups

ALTER TABLE menu_item ADD discount_on_order decimal(10) not null default 0; -- --- 注文時の割引額
UPDATE menu_item SET discount_on_order = 0;

ALTER TABLE menu_item ALTER COLUMN price_on_order RENAME TO fixed_on_order; -- 注文時の定価

# --- !Downs

ALTER TABLE menu_item DROP discount_on_order;
ALTER TABLE menu_item ALTER COLUMN fixed_on_order RENAME TO price_on_order;
