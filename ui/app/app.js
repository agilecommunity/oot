'use strict';

var transform = function(data){
    return jQuery.param(data);
}

angular.module('OotServices', ['ngResource'])
    .factory('User', ['$http', '$rootScope', function($http, $rootScope) {

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
    .factory('DailyMenu', ['$resource', function($resource){
        return $resource('/api/daily-menus', {}, {
            query: {
                  method: "GET"
                , isArray: true
                , transformResponse: function(data, headersGetter){
                    // 日付がLongでくるので、読める形に変換する
                    var list = angular.fromJson(data);
                    angular.forEach(list, function(item) {
                        item.menu_date = new Date(item.menu_date);
                    });
                    return list;
                }
            }
        });
    }])
    .factory('DailyOrder', ['$resource', function($resource){
        return $resource('/api/daily-orders/mine/:id', {id: "@id"}, {
            query: {
                method: "GET"
              , isArray: true
              , transformResponse: function(data, headersGetter){
                  // 日付がLongでくるので、読める形に変換する
                  var list = angular.fromJson(data);
                  angular.forEach(list, function(item) {
                      item.order_date = new Date(item.order_date);
                  });
                  return list;
              }
            }
            , create: {
                  method: "POST"
            }
            , update: {
                  method: "PUT"
                , isArray: false
            }
        });
    }]);


var app = angular.module('oot', [
              'ngRoute'
            , 'ngResource'
            , 'ui.bootstrap'
            , 'OotServices'
            ]);

app.config(['$routeProvider',
    function($routeProvider) {
        $routeProvider
            .when('/', {
                  templateUrl: '/views/signin'
                , controller:  'SigninController'
            })
            .when('/order', {
                  templateUrl: '/views/order'
                , controller:  'OrderController'
            })
            .otherwise({
                redirectTo: '/'
            });
    }]);

app.filter('getByMenuDate', function() {
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
    .filter('getByOrderDate', function() {
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
    })
    .filter('getById', function() {
        return function(input, filter_id) {
            var target = null;
            input.some(function(item) {
                if (item.id == filter_id) {
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
                    angular.forEach(response, function(order){
                        var menu = $filter('getByMenuDate')($scope.daily_menus, order.order_date);
                        if (menu != null) {
                            var item = $filter('getById')(menu.detail_items, order.detail_items[0].menu_item.id);
                            if (item != null) {
                                item.selected = true;
                            }
                        }
                    });

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

        $scope.order = function(daily_menu, daily_menu_item) {

            var new_state = daily_menu_item.selected != true;
            angular.forEach(daily_menu.detail_items, function(item) {
                item.selected = false;
            });
            daily_menu_item.selected = new_state;

            var order = $filter('getByOrderDate')($scope.daily_orders, daily_menu.menu_date);

            if (order != null) {
                if (new_state === true) {
                    order.detail_items = [{menu_item: daily_menu_item}];
                    order.$update();
                } else {
                    order.$delete();
                }
            } else {
                var new_order = new DailyOrder();
                new_order.order_date = daily_menu.menu_date.getTime();
                new_order.local_user = User.current_user();
                new_order.detail_items = [{menu_item: daily_menu_item}];

                DailyOrder.create({}, [new_order]);
            }

            $scope.daily_orders = DailyOrder.query();

        };

        $scope.showSideDishes = function() {
            $modal.open({
                  templateUrl: 'views/side-dishes'
            });
        };

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
