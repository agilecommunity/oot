
angular.module('MyControllers')
.controller('StartSignupController',
    ['$scope', '$location', '$http', 'dialogs',
    function ($scope, $location, $http, dialogs) {

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

        $http.post("/api/v1.0/start-signup", parameter)
        .success(function (data, status, header) {
            var dialog = dialogs.notify("メール送信完了", "登録したアドレスに確認メールを送りました");

            dialog.result["finally"](function(config){
                $location.path("/");
                $scope.$apply();
            });
        })
        .error(function (data, status, header) {
            $scope.formErrors = data.errors;
        });

    };

    }
]);

