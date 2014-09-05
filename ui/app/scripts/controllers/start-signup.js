
angular.module('MyControllers')
.controller('StartSignupController',
    ['$scope', '$location', '$http',
    function ($scope, $location, $http) {

    $scope.signup = function () {
        var parameter = {email: $scope.user_email};

        $http.post("/api/startSignup", parameter)
        .success(function (data, status, header) {
            alert(data);
        })
        .error(function (data, status, header) {
            alert(data);
        });

    };

    }
]);

