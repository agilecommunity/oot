
angular.module('MyControllers')
.controller('SignupController',
    ['$scope', '$location', '$routeParams', '$http',
    function ($scope, $location, $routeParams, $http) {

    $scope.signup = function () {
        var parameter = {};
        parameter.email = $scope.user_email;
        parameter.firstName = $scope.user_first_name;
        parameter.lastName = $scope.user_last_name;
        parameter.passWord1 = $scope.user_password;
        parameter.passWord2 = $scope.user_password_confirm;

        $http.post('/api/signup/' + $routeParams.token, parameter)
        .success(function (data, status, header) {
        })
        .error(function (data, status, header) {
        });

    };

    }
]);

