
angular.module('MyControllers')
.controller('SignupController',
    ['$scope', '$location', '$routeParams', '$http', 'dialogs',
    function ($scope, $location, $routeParams, $http, dialogs) {

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

        $http.post('/api/v1.0/signup/' + $routeParams.token, parameter)
        .success(function (data, status, header) {
            var dialog = dialogs.notify("サインアップ完了", "アカウントの登録が完了しました。サインインしてください");

            dialog.result["finally"](function(config){
                $location.path("/");
                $scope.$apply();
            });
        })
        .error(function (data, status, header) {
            switch (status) {
            case 422:
                $scope.formErrors = data.errors;
                break;
            case 403:
                dialogs.error("無効なリンク", data.message);
                break;
            default:
                var messages = [
                    "画面をリロードした後、再度操作を行ってみてください",
                    "問題が解消しない場合は管理者に連絡してください",
                    "",
                    "サーバ側のメッセージ: " + data.message
                ];
                dialogs.error("処理中にエラーが発生しました", messages.join("<br>"));
            }
        });
    };

    }
]);

