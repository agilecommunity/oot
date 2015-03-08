(function(){

    angular.module('MyControllers')
        .controller('StartResetPasswordController', StartResetPasswordController);

    StartResetPasswordController.$inject = ['$location', '$http', 'dialogs'];

    function StartResetPasswordController($location, $http, dialogs) {
        var vm = this;

        vm.email = null;
        vm.formErrors = {};

        vm.startReset = function() {
            vm.formErrors = {};
            var parameter = {email: vm.email};

            $http.post("/api/v1.0/start-reset", parameter)
            .success(function (data, status, header) {
                var dialog = dialogs.notify("メール送信完了", "入力したアドレスに確認メールを送りました");

                dialog.result["finally"](function(config){
                    $location.path("/");
                    vm.$apply();
                });
            })
            .error(function (data, status, header) {
                switch(status) {
                case 422:
                    vm.formErrors = data.errors;
                    break;
                default:
                    dialogs.error("初期化失敗", data.message);
                }
            });

        };

        vm.hasFormError = function(name) {
            return (vm.formErrors[name] !== null && vm.formErrors[name] !== undefined);
        };

        vm.getErrorClass = function(name) {
            return vm.hasFormError(name) ? "has-error" : "";
        };
    }

})();
