# --- First database schema

# --- !Ups

// ローカルユーザ
create table local_user (
  id                     varchar(255) not null, // --- ID
  provider               varchar(255) not null, // --- プロバイダ
  email                  varchar(255) not null, // --- メールアドレス
  password               varchar(255) not null, // --- ハッシュ化されたパスワード
  first_name             varchar(255) not null, // --- 姓
  last_name              varchar(255) not null, // --- 名
  is_admin               boolean default false, // --- 管理者かどうか

  created_at             timestamp,        // --- 作成日
  updated_at             timestamp,        // --- 最終更新日

  constraint pk_local_user primary key (id)
);

// ローカルトークン
create table local_token (
  uuid                   varchar(255) not null, // --- プロバイダ
  email                  varchar(255) not null, // --- メールアドレス

  created_at             timestamp,             // --- 作成日時
  expire_at              timestamp,             // --- 廃棄日時

  is_sign_up             boolean default false, // --- サインアップ中か?

  constraint pk_local_token primary key (uuid)
);

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists local_user;

drop table if exists local_token;

SET REFERENTIAL_INTEGRITY TRUE;
