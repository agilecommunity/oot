(function() {

    angular.module('MyControllers')
        .controller('AdminSettingsController', AdminSettingsController);

    AdminSettingsController.$inject = ["dialogs", "GatheringSetting", "initialData"];

    function AdminSettingsController(dialogs, GatheringSetting, initialData) {
        var vm = this;

        vm.errors = {};

        vm.gatheringSetting = initialData.gatheringSetting;

        vm.saveGathering = function() {

            var handler = {};

            handler.success = function(saved) {
                dialogs.notify("データ登録成功", "設定を保存しました");
            };

            handler.error = function(error) {
                console.log(error);
                if (error.status === 422) {
                    vm.errors.gatheringSetting = error.data.errors;
                } else {
                    var dialog = dialogs.error("データ登録・更新失敗", error.data.message);

                    dialog.result["finally"](function(){
                        // 何もしない
                    });
                }
            };

            vm.gatheringSetting.$update({}, handler.success, handler.error);
        };
    }

    app.my.resolvers.AdminSettingsController = {
        initialData: function($q, GatheringSetting) {
            var deferred = $q.defer();
            var initialData = {};

            GatheringSetting.get({
            }).$promise
            .then(function(value){
                initialData.gatheringSetting = value;

                deferred.resolve(initialData);
            })
            ["catch"](function(responseHeaders) {
                deferred.reject(responseHeaders);
            });

            return deferred.promise;
        }
    };

})();