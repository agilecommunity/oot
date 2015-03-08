(function(){

    angular.module('MyControllers')
        .controller('ResetPasswordController', ResetPasswordController);

    ResetPasswordController.$inject = ['$location', '$routeParams', '$http', 'dialogs'];

    function ResetPasswordController($location, $routeParams, $http, dialogs) {
        var vm = this;

        vm.passWord1 = null;
        vm.passWord2 = null;
        vm.formErrors = {};

        vm.reset = function() {
            vm.formErrors = {};
            var parameter = {};
            parameter.passWord1 = vm.passWord1;
            parameter.passWord2 = vm.passWord2;

            $http.post('/api/v1.0/reset/' + $routeParams.token, parameter)
            .success(function (data, status, header) {
                var dialog = dialogs.notify("初期化完了", "パスワードの初期化が完了しました。サインインしてください");

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
