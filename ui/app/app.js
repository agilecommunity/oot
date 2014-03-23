'use strict';

var transform = function(data){
    return jQuery.param(data);
}

angular.module('OotServices', ['ngResource'])
    .factory('User', ['$http', '$rootScope', function($http, $rootScope) {  // ユーザ認証を行うサービス

        $rootScope.current_user = null;

        return {

            current_user: function() {
               return $rootScope.current_user;
            }

          , is_signed_in: function() {
                return !($rootScope.current_user === null);
            }

          // ユーザ名、パスワードで認証を行う
          , signin: function(username, password, callback) {
                var parameter = {};
                parameter.username = username;
                parameter.password = password;

                $http({
                      method: 'POST'
                    , url: '/authenticate/userpass'
                    , headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' }
                    , transformRequest: transform
                    , data: parameter
                })
                .success(function(data, status, header){
                    $rootScope.current_user = data;
                    callback['success']();
                })
                .error(function(data, status, header){
                    $rootScope.current_user = null;
                    callback['error'](status);
                });
            }

          // 取得しているトークンで認証情報を所得してみる
          , re_signin: function(callback) {

              $http.get("/api/users/me")
              .success(function(data, status, header){
                  $rootScope.current_user = data;
                  callback['success']();
              })
              .error(function(data, status, header){
                  $rootScope.current_user = null;
                  callback['error']();
              })
          }

        };
    }])
    .factory('DailyMenu', ['$resource', function($resource){  // 日々のメニューを扱うサービス
                                                   // ここだけ丁寧に解説
        return $resource(                          // RESTのAPIを簡単に扱える$resourceサービスを利用する
              '/api/daily-menus/:id'               // APIのURL。:idは変数 query,createなど必要のないときは使われない
            , {id: "@id"}, {                       // :idを@idにマッピングする。@はオブジェクトのプロパティを意味するので、
                                                   // DailyMenuオブジェクトのプロパティ"id"の値が使われる
              query: {                             // queryはオブジェクト全件を取り出す
                  method: "GET"
                , isArray: true                    // 結果が配列になる場合は必ずtrueにする(でないと、エラーが発生する)
                , transformResponse: function(data, headersGetter){  // 結果を変換したい場合はtransformResponseを使う
                    // 日付が数字でくるとDateに変換されないので、こちらで変換する
                    var list = angular.fromJson(data);
                    angular.forEach(list, function(item) {
                        item.menu_date = new Date(item.menu_date);
                    });
                    return list;
                }
            }
        });
    }])
    .factory('DailyOrder', ['$resource', function($resource){  // 日々の注文を扱うサービス
        return $resource('/api/daily-orders/mine/:id', {id: "@id"}, {
            query: {
                method: "GET"
              , isArray: true
              , transformResponse: function(data, headersGetter){
                  // 日付が数字でくるとDateに変換されないので、こちらで変換する
                  var list = angular.fromJson(data);
                  angular.forEach(list, function(item) {
                      item.order_date = new Date(item.order_date);
                  });
                  return list;
              }
            }
            , create: {                // 新規作成
                  method: "POST"
            }
            , update: {                // 更新
                  method: "PUT"
                , isArray: false
                , transformRequest: function(data, headersGetter){
                    // 日付を数値に変換する
                    data.order_date = data.order_date.getTime();
                    return angular.toJson(data);
                }
            }
        });
    }]);


var app = angular.module('oot', [  // アプリケーションの定義
              'ngRoute'            // 依存するサービスを指定する
            , 'ngResource'
            , 'ui.bootstrap'
            , 'OotServices'        // 自分が作ったサービス
            ]);

app.config(['$routeProvider',      // ルーティングの定義
    function($routeProvider) {
        $routeProvider
            .when('/', {                           // AngularJS上でのパス
                  templateUrl: '/views/signin'     // 利用するビュー
                , controller:  'SigninController'  // 利用するコントローラー
            })
            .when('/order', {
                  templateUrl: '/views/order'
                , controller:  'OrderController'
            })
            .otherwise({                           // その他のパスが指定された場合
                redirectTo: '/'                    // "/"に飛ぶ
            });
    }]);

app.filter(                                        // フィルタの定義。コントローラーらサービスで利用できる
        'getByMenuDate', function() {              // menu_dateによる検索(フィルタとして定義するのが正しいのか疑問)
        return function(input, filter_date) {
            var target = null;
            input.some(function(item) {
                if (item.menu_date.valueOf() == filter_date.valueOf()) {
                    target = item;
                }
                return target != null;
            });
            return target;
        }
    })
    .filter('getByOrderDate', function() {         // order_dateによる検索
        return function(input, filter_date) {
            var target = null;
            input.some(function(item) {
                if (item.order_date.valueOf() == filter_date.valueOf()) {
                    target = item;
                }
                return target != null;
            });
            return target;
        }
    });

app.controller('SigninController', ['$scope', '$location', 'User', function($scope, $location, User) {
        $scope.signin = function() {
            User.signin($scope.user.email, $scope.user.password, {
                  success: function(){
                    $location.path("/order");
                }
                , error: function(status){
                    alert("サインインに失敗しました。 status:" + status);
                }
            });
        };
    }])
    .controller('OrderController', ['$scope', '$modal', '$location', '$filter', 'User', 'DailyMenu', 'DailyOrder'
                                 , function($scope, $modal, $location, $filter, User, DailyMenu, DailyOrder) {

        $scope.daily_menus = DailyMenu.query({}
            , function(response){ // 成功時

                $scope.daily_orders = DailyOrder.query({}
                , function(response){ // 成功時
                }
                , function(response){   // 失敗時
                    alert("注文データが取得できませんでした。サインイン画面に戻ります。");
                    $location.path("/");
                });

            }
            , function(response){   // 失敗時
                alert("メニューのデータが取得できませんでした。サインイン画面に戻ります。");
                $location.path("/");
            });

        $scope.order = function(daily_menu, daily_menu_item) { // イベントハンドラ

            // メニューの注文状況を切り替える
            var new_state = daily_menu_item.ordered != true;
            daily_menu_item.ordered = new_state;

            // 注文オブジェクトがあるかどうかを調べる
            var order = $filter('getByOrderDate')($scope.daily_orders, daily_menu.menu_date);

            var reload_orders = function(request) {
                // 念のためサーバから最新の注文を取得する
                $scope.daily_orders = DailyOrder.query();
            };

            if (order != null) {
                if (new_state === true) {
                    order.detail_items.push({menu_item: daily_menu_item.menu_item});
                } else {
                    var work = order.detail_items.filter(function(item, index){
                        return (item.menu_item.id !== daily_menu_item.menu_item.id);
                    });
                    order.detail_items = work;
                }

                // あった場合は更新する
                if (order.detail_items.length > 0) {
                    order.$update({}, reload_orders);
                } else {
                    order.$delete({}, reload_orders);
                }
            } else {
                // ない場合は新しく作る
                var new_order = new DailyOrder();
                new_order.order_date = daily_menu.menu_date.getTime();
                new_order.local_user = User.current_user();
                new_order.detail_items = [{menu_item: daily_menu_item.menu_item}];

                DailyOrder.create({}, [new_order], reload_orders);
            }
        };

        $scope.showSideDishes = function() { // イベントハンドラ
            $modal.open({
                  templateUrl: 'views/side-dishes'
            });
        };

        // メニューに注文状況を反映する
        var applyOrdered = function() {
            // メニューの注文状況をリセットする
            angular.forEach($scope.daily_menus, function(menu){
                angular.forEach(menu.detail_items, function(item){
                    item.ordered = false;
                })
            });
            // 注文を見ながらメニューの注文状況を変更する
            angular.forEach($scope.daily_orders, function(order){
                var menu = $filter('getByMenuDate')($scope.daily_menus, order.order_date);
                if (menu != null) {
                    angular.forEach(order.detail_items, function(o_d_item){
                        angular.forEach(menu.detail_items, function(m_d_item){
                            if (o_d_item.menu_item.id === m_d_item.menu_item.id) {
                                m_d_item.ordered = true;
                            }
                        });
                    });
                }
            });
        }

        // メニュー、または注文の内容が変わった場合は、メニューの注文状況を反映しなおす
        $scope.$watchCollection("daily_menus", applyOrdered);
        $scope.$watchCollection("daily_orders", applyOrdered);

    }]);

app.run(function($rootScope, $http, $location, User){
    // ブラウザのリロード対策
    $rootScope.$on('$locationChangeStart', function(ev, next, current) {

        var nextParam = jQuery('<a>', { href: next } )[0];

        // サインイン画面の場合は何もしない
        if (nextParam.pathname === "/" && (nextParam.hash === "#/" || nextParam.hash === "")) {
            return;
        }

        // すでに認証済みの場合は何もしない
        if (User.is_signed_in()) {
            return;
        }

        // 現在持っているトークンを使って再認証する
        User.re_signin({
              success: function() {
                  // 何もしない
              }
            , error: function() {
                ev.preventDefault();
                $location.path("/");
            }
        });
    });
});
