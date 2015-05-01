(function(){

    angular.module('MyControllers')
        .controller('MenuItemEditController', MenuItemEditController);

    MenuItemEditController.$inject = ['$scope', '$location', '$filter', 'dialogs', 'MenuItem', 'menuItem'];

    function MenuItemEditController($scope, $location, $filter, dialogs, MenuItem, menuItem) {

        var vm = this;

        vm.menuItem = menuItem;
        var menuItemSaved = angular.copy(vm.menuItem);

        vm.errors = [];

        var createItem = function(menuItem) {

            var handler = {};

            handler.success = function(saved) {
                $scope.$close(saved[0]);
            };

            handler.error = function(error) {
                console.log(error);
                if (error.status == 422) {
                    vm.errors = error.data.errors[0];
                } else {
                    var dialog = dialogs.error("データ登録・更新失敗", error.data.message);

                    dialog.result["finally"](function(){
                        $scope.$close();
                    });
                }
            };

            MenuItem.create({}, [menuItem], handler.success, handler.error);
        };

        var updateItem = function(menuItem) {

            var handler = {};

            handler.success = function(saved) {
                $scope.$close(saved);
            };

            handler.error = function(error) {
                console.log(error);
                if (error.status === 422) {
                    vm.errors = error.data.errors;
                } else {
                    var dialog = dialogs.error("データ登録・更新失敗", error.data.message);

                    dialog.result["finally"](function(){
                        $scope.$close();
                    });
                }
            };

            menuItem.$update({}, handler.success, handler.error);
        };

        vm.save = function() {
            if (vm.menuItem.id === undefined) {
                createItem(vm.menuItem);
            } else {
                updateItem(vm.menuItem);
            }
        };

        vm.cancel = function() {
            angular.copy(menuItemSaved, vm.menuItem);
            $scope.$dismiss();
        };

        vm.setCategory = function(value) {
            vm.menuItem.category = value;
        };

        vm.setStatus = function(value) {
            vm.menuItem.status = value;
        };

        vm.hasError = function(name) {
            if (vm.errors.length === 0) {
                return false;
            }

            return (vm.errors[name] !== undefined && vm.errors[name] !== null);
        };

    }

})();
