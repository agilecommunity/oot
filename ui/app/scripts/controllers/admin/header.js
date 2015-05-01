(function() {

    angular.module('MyControllers')
        .controller('AdminHeaderController', AdminHeaderController);

    AdminHeaderController.$inject = ['$location'];

    function AdminHeaderController($location) {
        var vm = this;

        //---- ヘルパ
        vm.isActive = function(url) {
            return $location.path() === url;
        };

        //---- イベントハンドラ
        vm.showAdminIndex = function () {
            $location.path("/admin/index");
        };

        vm.showDailyMenuEdit = function () {
            $location.path("/admin/daily-menus");
        };

        vm.showMenuItemsIndex = function () {
            $location.path("/admin/menu-items/index");
        };

        vm.showMenuItemsImport = function () {
            $location.path("/admin/menu-items/import");
        };

        vm.showMenuItemImagesImport = function () {
            $location.path("/admin/menu-item-images/import");
        };

        vm.showUsersIndex = function () {
            $location.path("/admin/users/index");
        };

        vm.showSettings = function () {
            $location.path("/admin/settings");
        };
    }

})();
