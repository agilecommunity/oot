(function(){

    angular.module('MyControllers')
        .controller('DailyMenuSelectItemController', DailyMenuSelectItemController);

    DailyMenuSelectItemController.$inject = ['$scope', '$location', '$filter', '$modal', 'usSpinnerService', 'MenuItem', 'Assets', 'category', 'selectedItems', 'currentItem'];

    function DailyMenuSelectItemController($scope, $location, $filter, $modal, usSpinnerService, MenuItem, Assets, category, selectedItems, currentItem) {

        var vm = this;

        console.log("DailyMenuSelectItemController category: " + category);
        console.log("DailyMenuSelectItemController selectedItems: ");
        console.log(selectedItems);
        console.log("DailyMenuSelectItemController currentItem: ");
        console.log(currentItem);

        var startBlock = function() {
            $.blockUI({baseZ: 2000, message: null});
            usSpinnerService.spin("spinner");
        };

        var stopBlock = function() {
            usSpinnerService.stop("spinner");
            $.unblockUI();
        };

        var showItems = function() {
            var filterShop = vm.filters.shop;
            startBlock();
            MenuItem.queryByShopName({shopName: filterShop.name, status: 'valid'},
                function (response) { // 成功時
                    vm.menuItems = $filter('filter')(response, {category: category});
                    stopBlock();
                },
                function (response) {   // 失敗時
                    alert("データが取得できませんでした。サインイン画面に戻ります。");
                    stopBlock();
                    $scope.$dismiss();
                    $location.path("/");
                });
        };

        vm.menuItems = [];

        vm.filters = {};
        vm.filters.shop = {id: '@none', name: '選択してください'};

        vm.selectShops = function() {
            var modalInstance = $modal.open({
                templateUrl: Assets.versioned("/views/admin/daily-menu/select-shop"),
                controller: "DailyMenuSelectShopController",
                controllerAs: "vm"
            });

            modalInstance.result.then(function (selectedItem) {
                vm.filters.shop = selectedItem;
                showItems();
            }, function () {
                console.log('Modal dismissed at: ' + new Date());
            });
        };

        vm.isEmpty = function() {
            return vm.menuItems.length === 0;
        };

        vm.isSelected = function(item) {
            var selected = ($filter('getById')(selectedItems, item.id) !== null);
            return (selected && item.id !== currentItem.id);
        };

        vm.selectThis = function(item) {
            if (vm.isSelected(item)) {
                return;
            }
            $scope.$close(item);
        };

        vm.renderFilterShop = function() {
            return vm.filters.shop.name;
        };
    }

})();