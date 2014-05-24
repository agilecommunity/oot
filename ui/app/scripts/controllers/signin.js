
angular.module('MyControllers')
.controller('SigninController',
    ['$scope', '$location', 'User',
    function ($scope, $location, User) {

    $scope.signin = function () {
        User.signin($scope.user_email, $scope.user_password, {
            success: function () {
                var path = "/order";
                if (User.current_user().is_admin === true) { // 管理者の場合は管理インデックスに飛ばす
                    path = "/admin/index";
                }
                $location.path(path);
            },
            error: function (status) {
                alert("サインインに失敗しました。 status:" + status);
            }
        });
    };

    }
]);

