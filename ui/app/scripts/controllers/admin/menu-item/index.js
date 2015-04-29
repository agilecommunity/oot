
angular.module('MyControllers')
    .controller('MenuItemIndexController',
    ['$scope', '$location', '$routeParams', '$filter', '$modal', 'usSpinnerService', 'User', 'MenuItem', 'Assets',
    function ($scope, $location, $routeParams, $filter, $modal, usSpinnerService, User, MenuItem, Assets) {

    var startBlock = function() {
        $.blockUI({baseZ: 2000, message: null});
        usSpinnerService.spin("spinner");
    };

    var stopBlock = function() {
        usSpinnerService.stop("spinner");
        $.unblockUI();
    };

    var showItems = function() {
        var filterShop = $scope.filters.shop;
        startBlock();
        $scope.menuItems = MenuItem.queryByShopName({shopName: filterShop.name},
            function (response) { // 成功時
                stopBlock();
            },
            function (response) {   // 失敗時
                alert("データが取得できませんでした。サインイン画面に戻ります。");
                stopBlock();
                $scope.$dismiss();
                $location.path("/");
            });
    };

    var setUp = function() {
        $scope.menuItems = [];

        $scope.filters = {};
        $scope.filters.shop = {id: '@none', name: '選択してください'};
    };

    $scope.selectShops = function() {
        var modalInstance = $modal.open({
            templateUrl: Assets.versioned("/views/admin/daily-menu/select-shop"),
            controller: "DailyMenuSelectShopController"
        });

        modalInstance.result.then(function (selectedItem) {
            $scope.filters.shop = selectedItem;
            showItems();
        }, function () {
            console.log('Modal dismissed at: ' + new Date());
        });
    };

    $scope.editItem = function(menuItem) {
        var modalInstance = $modal.open({
            templateUrl: Assets.versioned("/views/admin/menu-item/edit"),
            controller: "MenuItemEditController",
            backdrop: "static",
            resolve: {
                menuItem: function() {
                    return menuItem;
                }
            }
        });
    };

    $scope.addItem = function() {
        var modalInstance = $modal.open({
            templateUrl: Assets.versioned("/views/admin/menu-item/edit"),
            controller: "MenuItemEditController",
            backdrop: "static",
            resolve: {
                menuItem: function() {
                    return new MenuItem();
                }
            }
        });

        modalInstance.result.then(function (newItem){
            console.log(newItem);
            if (newItem !== undefined) {
                $scope.menuItems.push(newItem);
            }
        });
    };

    $scope.renderFilterShop = function() {
        return $scope.filters.shop.name;
    };

    setUp();
}]);