# 開発に関する情報

## 環境

 * msysgit
 * Play framework
   * JDK 1.7.x
 * Node.js
 * npm
 * yoman


### インストール

#### msysgit

[Downloads - msysgit - Git for Windows - Google Project Hosting](http://code.google.com/p/msysgit/downloads/list)  
Git-x.x.x-preview... を選んで大丈夫。

#### Play Framework

[Download Play Framework](http://www.playframework-ja.org/download)  
Zipでもどっちでも可。


#### Node.js, npm

[node.js](http://nodejs.org/download/)  
Windows Installerを選べばよし。入れると一緒にnpmも入る。

#### yoman

> npm install -g yo

でインストール完了。

## 規則

### 命名規則

クラス名   : CamelCase
メソッド名 : lowerCamelCase
メンバ名   : snake_case // 列名と合わせる

テーブル名 : snake_case
列名       : snake_case

API名      : spinal-case


## 設計, 実装

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
