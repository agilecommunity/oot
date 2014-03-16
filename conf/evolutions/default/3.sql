# --- First database schema

# --- !Ups

// 日々の注文
create table daily_order (
  id                     bigint not null,       // --- ID

  order_date             date not null,         // --- 日付
  user_id                varchar(255) not null, // --- 注文者のID

  created_at             timestamp,        // --- 作成日
  updated_at             timestamp,        // --- 最終更新日

  constraint pk_daily_order primary key (id)
);

// 日々の注文の内容
create table daily_order_item (
  id               bigint not null,  // --- ID
  daily_order_id   bigint not null,  // --- 注文ID
  menu_item_id     bigint not null,  // --- 弁当ID

  created_at             timestamp,        // --- 作成日
  updated_at             timestamp,        // --- 最終更新日

  constraint pk_daily_order_item primary key (id)
);

create sequence daily_order_seq start with 1;
create sequence daily_order_item_seq start with 1;

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists daily_order;

drop table if exists daily_order_item;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists daily_order_seq;

drop sequence if exists daily_order_item_seq;

