(function(){

    angular.module('MyControllers')
        .controller('SigninController', SigninController);

    SigninController.$inject = ['$location', 'ipCookie', 'dialogs', 'User'];

    function SigninController($location, ipCookie, dialogs, User) {

        var vm = this;

        vm.register_email = ipCookie("signin.register_email");
        vm.user_email = ipCookie("signin.user_email");
        vm.user_password = null;
        vm.errors = [];

        var saveCookie = function() {
            var cookieOptions = {expires: 90}; // 90日(ログインするたびに更新するからこれくらいで大丈夫だろう)

            ipCookie("signin.register_email", vm.register_email, cookieOptions);
            if (vm.register_email) {
                ipCookie("signin.user_email", vm.user_email, cookieOptions);
            } else {
                ipCookie.remove("signin.register_email");
                ipCookie.remove("signin.user_email");
            }
        };

        vm.signin = function () {

            vm.errors = [];

            saveCookie();

            User.signin(vm.user_email, vm.user_password, {
                success: function () {
                    var path = "/order";
                    if (User.currentUser().isAdmin === true) { // 管理者の場合は管理インデックスに飛ばす
                        path = "/admin/index";
                    }
                    $location.path(path);
                },
                error: function (result) {
                    if (result.data.username !== undefined || result.data.password !== undefined) {
                        vm.errors = result.data;
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

        vm.hasError = function(name) {
            if (vm.errors.length === 0) {
                return false;
            }

            return (vm.errors[name] !== undefined && vm.errors[name] !== null);
        };
    }

    app.my.resolvers.SigninController = {
        initialData: function($q, User) {

            var deferred = $q.defer();
            var initialData = {};

            // データはないけど、
            User.signout({
                success: function(){
                    deferred.resolve(initialData);
                },
                error: function(responseHeaders){
                    deferred.reject({status: responseHeaders.status, reason: responseHeaders.data});
                }
            });

            return deferred.promise;
        }
    };

})();
