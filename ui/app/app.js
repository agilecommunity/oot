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

          , login: function(username, password, success, error) {
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
                    success();
                })
                .error(function(data, status, header){
                    error(status);
                });
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
            User.login($scope.user.email, $scope.user.password
                , function(){
                    $location.path("/order");
                }
                , function(status){
                    alert("login error status:" + status);
                });
        };
    }])
    .controller('OrderController', ['$scope', '$http', '$modal', function($scope, $http, $modal) {

        $http.get('/api/daily_menus').success(function(data) {
            $scope.daily_menus = data;

            angular.forEach($scope.daily_menus, function(daily_menu) {
                daily_menu.menu_date = jQuery.format.date(new Date(daily_menu.menu_date), 'yyyy/MM/dd (ddd)');
            });
        });

        $scope.showSideDishes = function() {
            $modal.open({
                  templateUrl: 'views/side-dishes'
            });
        };
    }]);
