
angular.module('MyControllers')
.controller('SignupController',
    ['$scope', '$location', '$routeParams', '$http',
    function ($scope, $location, $routeParams, $http) {

    $scope.formErrors = {};

    $scope.hasFormError = function(name) {
        return ($scope.formErrors[name] !== null && $scope.formErrors[name] !== undefined);
    };

    $scope.getErrorClass = function(name) {
        return $scope.hasFormError(name) ? "has-error" : "";
    };

    $scope.signup = function () {
        $scope.formErrors = {};

        var parameter = {};
        parameter.email = $scope.email;
        parameter.firstName = $scope.firstName;
        parameter.lastName = $scope.lastName;
        parameter.passWord1 = $scope.passWord1;
        parameter.passWord2 = $scope.passWord2;

        $http.post('/api/signup/' + $routeParams.token, parameter)
        .success(function (data, status, header) {
            bootbox.dialog({
                message: "アカウントの登録が完了しました。サインインしてください",
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
            switch (status) {
                case 400:
                    $scope.formErrors = data;
                    break;
                default:
                    bootbox.dialog({
                        message: data,
                        closeButton: false,
                        buttons: {
                            success: {
                                label: "OK",
                                className: "btn-success"
                            }
                        }
                    });
            }
        });
    };

    }
]);

