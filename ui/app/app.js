'use strict';

var app = angular.module('oot', [
              'ngRoute'
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
            })
            .otherwise({
                redirectTo: '/'
            });
    }]);

app.controller('SigninController', ['$scope', '$location', function($scope, $location) {
        $scope.signin = function() {
            $location.path("/order");
        };
    }]);

