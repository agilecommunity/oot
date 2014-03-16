'use strict';

var transform = function(data){
    return jQuery.param(data);
}

angular.module('OotServices', [])
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
    }]);

var app = angular.module('oot', [
              'ngRoute'
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
    .controller('OrderController', ['$scope', '$http', '$modal', '$location', function($scope, $http, $modal, $location) {

        $http.get('/api/daily_menus')
        .success(function(data) {
            $scope.daily_menus = data;

            angular.forEach($scope.daily_menus, function(daily_menu) {
                daily_menu.menu_date = jQuery.format.date(new Date(daily_menu.menu_date), 'yyyy/MM/dd (ddd)');
            });
        })
        .error(function(data, status, header){
            alert("メニューのデータが取得できませんでした。サインイン画面に戻ります。status:" + status);
            $location.path("/");
        });

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
