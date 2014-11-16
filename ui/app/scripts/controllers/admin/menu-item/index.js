
angular.module('MyControllers')
    .controller('MenuItemIndexController',
    ['$scope', '$location', '$routeParams', '$filter', '$modal', 'usSpinnerService', 'User', 'MenuItem',
    function ($scope, $location, $routeParams, $filter, $modal, usSpinnerService, User, MenuItem) {

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

    $scope.menuItems = [];

    $scope.filters = {};
    $scope.filters.shop = {id: '@none', name: '選択してください'};

    $scope.selectShops = function() {
        var modalInstance = $modal.open({
            templateUrl: "/views/admin/daily-menu/select-shop",
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
        $scope.menuItem = menuItem;
        var modalInstance = $modal.open({
            templateUrl: "/views/admin/menu-item/edit",
            scope: $scope,
            controller: "MenuItemEditController",
            backdrop: "static"
        });
    };

    $scope.addItem = function() {
        $scope.menuItem = new MenuItem();
        var modalInstance = $modal.open({
            templateUrl: "/views/admin/menu-item/edit",
            scope: $scope,
            controller: "MenuItemEditController",
            backdrop: "static"
        });

        modalInstance.result.then(function (){
            $scope.menuItems.push($scope.menuItem);
        });
    };

    $scope.renderFilterShop = function() {
        return $scope.filters.shop.name;
    };

}]);