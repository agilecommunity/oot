'use strict';

var app = angular.module('oot', [
              'ngRoute'
            , 'ui.bootstrap'
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

app.controller('SigninController', ['$scope', '$location', function($scope, $location) {
        $scope.signin = function() {
            $location.path("/order");
        };
    }])
    .controller('OrderController', ['$scope', '$http', '$modal', function($scope, $http, $modal) {
        $scope.showSideDishes = function() {
            $modal.open({
                  templateUrl: 'views/side-dishes'
            });
        };
    }]);

