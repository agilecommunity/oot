# --- First database schema

# --- !Ups

// メニューに出す品
create table menu_item (
  id                     bigint not null,       // --- ID
  category               varchar(10) not null,  // --- カテゴリ (bento=弁当, side=デザートやサラダ)
  shop_name              varchar(255) not null, // --- 店名
  name                   varchar(255) not null, // --- 名前
  price_on_order         decimal(10) not null,  // --- 注文価格
  item_image_path        varchar(1024),         // --- 画像のファイルパス

  created_at             timestamp,        // --- 作成日
  updated_at             timestamp,        // --- 最終更新日

  constraint pk_menu_item primary key (id)
);

// 日々のメニュー
create table daily_menu (
  id                     bigint not null,  // --- ID
  menu_date              date not null,    // --- 日付
  status                 varchar(10) default 'prepared',  // --- ステータス: prepared, open, closed

  created_at             timestamp,        // --- 作成日
  updated_at             timestamp,        // --- 最終更新日

  constraint pk_daily_menu primary key (id)
);

// 日々のメニューで注文できる品
create table daily_menu_item (
  id               bigint not null,  // --- ID
  daily_menu_id    bigint not null,  // --- メニューID
  menu_item_id     bigint not null,  // --- 弁当ID

  created_at             timestamp,        // --- 作成日
  updated_at             timestamp,        // --- 最終更新日

  constraint pk_daily_menu_item primary key (id)
);

create sequence menu_item_seq start with 1;
create sequence daily_menu_seq start with 1;
create sequence daily_menu_item_seq start with 1;

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists menu_item;

drop table if exists daily_menu;

drop table if exists daily_menu_item;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists menu_item_seq;

drop sequence if exists daily_menu_seq;

drop sequence if exists daily_menu_item_seq;

