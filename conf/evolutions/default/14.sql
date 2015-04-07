# --- First database schema

# --- !Ups

-- ギャザリングの設定
create table gathering_setting (
  id                     bigint not null,       -- --- ID

  enabled                boolean default false, -- --- 有効にするかどうか
  min_orders             smallint,              -- --- 目標注文件数
  discount_price         decimal(10) not null,  -- --- 割引額

  created_at             timestamp,             -- --- 作成日
  updated_at             timestamp,             -- --- 最終更新日

  created_by             varchar(255) not null, -- --- 作成者のID
  updated_by             varchar(255) not null, -- --- 最終更新者のID

  constraint pk_gathering_setting primary key (id)
);

create sequence gathering_setting_seq start with 1;

INSERT INTO gathering_setting VALUES (1, false, 0, 0, '2015-04-07', '2014-04-07', 'system', 'system');

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists gathering_setting;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists gathering_setting_seq;

