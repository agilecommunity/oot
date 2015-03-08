(function(){

    angular.module('MyControllers')
        .controller('StartResetController', StartResetController);

    StartResetController.$inject = ['$location', '$http', 'dialogs'];

    function StartResetController($location, $http, dialogs) {
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
                vm.formErrors = data.errors;
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
