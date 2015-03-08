
angular.module('MyControllers')
.controller('SigninController',
    ['$scope', '$location', 'dialogs', 'User',
    function ($scope, $location, dialogs, User) {

    $scope.errors = [];

    $scope.signin = function () {
        $scope.errors = [];
        User.signin($scope.user_email, $scope.user_password, {
            success: function () {
                var path = "/order";
                if (User.currentUser().isAdmin === true) { // 管理者の場合は管理インデックスに飛ばす
                    path = "/admin/index";
                }
                $location.path(path);
            },
            error: function (result) {
                if (result.data.username !== undefined || result.data.password !== undefined) {
                    $scope.errors = result.data;
                } else {
                    var messages;
                    switch (result.status) {
                    case 400:
                        messages = [
                            "サインインに失敗しました",
                            "メールアドレスまたはパスワードに誤りがあります"
                        ];
                        dialogs.error("サインイン失敗", messages.join("<br>"));
                        break;
                    default:
                        messages = [
                            "サインインに失敗しました",
                            "画面をリロードした後、再度操作を行ってみてください",
                            "問題が解消しない場合は管理者に連絡してください",
                            "",
                            "サーバ側のメッセージ: status:" + result.status
                        ];
                        dialogs.error("サインイン失敗", messages.join("<br>"));
                        break;
                    }
                }
            }
        });
    };

    $scope.hasError = function(name) {
        if ($scope.errors.length === 0) {
            return false;
        }

        return ($scope.errors[name] !== undefined && $scope.errors[name] !== null);
    };
}]);

