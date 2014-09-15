
angular.module('MyControllers')
.controller('StartSignupController',
    ['$scope', '$location', '$http',
    function ($scope, $location, $http) {

    $scope.formErrors = {};

    $scope.hasFormError = function(name) {
        return ($scope.formErrors[name] !== null && $scope.formErrors[name] !== undefined);
    };

    $scope.getErrorClass = function(name) {
        return $scope.hasFormError(name) ? "has-error" : "";
    };

    $scope.signup = function () {

        $scope.formErrors = {};
        var parameter = {email: $scope.email};

        $http.post("/api/startSignup", parameter)
        .success(function (data, status, header) {
            bootbox.dialog({
                message: "登録したアドレスに確認メールを送りました",
                closeButton: false,
                buttons: {
                    success: {
                        label: "OK",
                        className: "btn-success",
                        callback: function () {
                            $location.path("/");
                            $scope.$apply();
                        }
                    }
                }
            });
        })
        .error(function (data, status, header) {
            $scope.formErrors = data;
        });

    };

    }
]);

