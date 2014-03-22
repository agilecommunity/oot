'use strict';

var transform = function(data){
    return jQuery.param(data);
}

angular.module('OotServices', ['ngResource'])
    .factory('User', ['$http', '$rootScope', function($http, $rootScope) {

        $rootScope.current_user = null;

        return {

            is_signed_in: function() {
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
                    var menus = angular.fromJson(data);
                    angular.forEach(menus, function(daily_menu) {
                        daily_menu.menu_date = new Date(daily_menu.menu_date);
                    });
                    return menus;
                }
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
    .controller('OrderController', ['$scope', '$modal', '$location', 'DailyMenu', function($scope, $modal, $location, DailyMenu) {

        $scope.daily_menus = DailyMenu.query({}
            , function(response){}  // 成功時
            , function(response){   // 失敗時
                alert("メニューのデータが取得できませんでした。サインイン画面に戻ります。status:" + response.status);
                $location.path("/");
            });

        $scope.showSideDishes = function() {
            $modal.open({
                  templateUrl: 'views/side-dishes'
            });
        };

        $scope.order = function(daily_menu, daily_item) {
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
