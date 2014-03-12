'use strict';

var app = angular.module('oot', [
              'ngRoute'
            ]);

app.config(['$routeProvider',
    function($routeProvider) {
        $routeProvider
            .when('/', {
                  templateUrl: '/views/signin'
            })
            .when('/order', {
                  templateUrl: '/views/order'
            })
            .otherwise({
                redirectTo: '/'
            });
    }]);

