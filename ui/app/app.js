'use strict';

angular.module('OotServices', [])
    .factory('User', ['$http', '$rootScope', function($http, $rootScope) {
        return {
            login: function() {
                alert("login");
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
            User.login();
            $location.path("/order");
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

