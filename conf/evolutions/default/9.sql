# --- !Ups

ALTER TABLE daily_order_item ADD num_orders smallint; -- --- 注文数
UPDATE daily_order_item SET num_orders = 1;

# --- !Downs

ALTER TABLE daily_order_item DROP num_orders;
