# 美味しいお弁当を食べたい

## 想定している環境

 * Play Framework 2.2.x
 * Java 1.7.x

## 実行方法

以下を実行後、 http://localhost:9000/ にアクセス


```
git clone https://github.com/nobiinu-and/oot
play run
```

## 開発情報

### 命名規則

クラス名   : CamelCase
メソッド名 : lowerCamelCase
メンバ名   : snake_case // 列名と合わせる

テーブル名 : snake_case
列名       : snake_case

API名      : spinal-case

### 認証

SecureSocialを利用
現在は userpass しか対応させていない

 * 本家: [SecureSocial - Authentication for Play Framework Applications](http://securesocial.ws/)
 * インストール: [SecureSocial導入しました。（導入までのチュートリアル） | Noriaki Horiuchi Tech Blog](http://tech.noriakihoriuchi.com/securesocialdao-ru-shimashita-dao-ru-madenochiyutoriaru)
 * Serviceの実装: [playframework - Play SecureSocial Persistance with Java - Stack Overflow](http://stackoverflow.com/questions/16093023/play-securesocial-persistance-with-java)

### 脆弱性対策

#### CSRF

Cookieトークンを使ってチェックする
Cookieトークンの名前は AngularJS に準拠する

 * Play: CookieにXSRF-TOKENを付与
 * AngularJS: CookieからXSRF-TOKENを取り出し、HTTPヘッダにX-XSRF-TOKENを付与
 * Play: XSRF-TOKENと、X-XSRF-TOKENの値を使って検証

 * Play Framework側
   * @AddCSRFToken, @RequireCSRFCheck4Ng アノテーションを利用する。
   * Globalオブジェクトのフィルタで付与する方法もあるが、TOKENのhttpOnlyがTrueになってしまい、AngularJSが参照できなくなるためそちらは利用しない
   * トークンの設定
     * Application.confで設定
   * クライアントからの応答の検証
     * HTTPヘッダのX-XSRF-TOKENからトークンを取得し、チェックを行うよう、カスタムのフィルタを作成する
       * @RequireCSRFCheck4Ng を付与すれば自動的に行う
 * AngularJS側
   * 何もしない。

 * [3分で分かるAngularJSセキュリティ - teppeis blog](http://teppeis.hatenablog.com/entry/2013/12/angularjs-security)
 * [PHPのイタい入門書を読んでAjaxのXSSについて検討した(3)～JSON等の想定外読み出しによる攻撃～ - ockeghem(徳丸浩)の日記](http://d.hatena.ne.jp/ockeghem/20110907/p1)
 * [JavaCsrf](http://www.playframework.com/documentation/2.2.x/JavaCsrf)

### AngularJS の参考情報

#### 仕組み

 * [AngularJSを使ったWebアプリのアーキテクチャ設計 - Qiita](http://qiita.com/zoetro/items/46d2a8b57f2645bb5033)
 * [AngularJSのMVWパターンを理解する - Qiita](http://qiita.com/zoetro/items/a45dbc18bb2b22e944b2)

#### ガイド

 * [AngularJS: Developer Guide: Angular Services: Using $location](http://docs.angularjs.org/guide/dev_guide.services.$location)

#### 拡張

 * UI作るのが便利になるBootstrap
   [Angular directives for Bootstrap](http://angular-ui.github.io/bootstrap/)

#### TIPS

 * [AngularJSでロード中に評価前のマークアップを表示させない方法 - Qiita](http://qiita.com/emalock/items/da681b7ba6a3828835f5)

