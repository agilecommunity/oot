
angular.module('MyControllers')
    .controller('MenuItemIndexController',
    ['$scope', '$location', '$routeParams', '$filter', '$modal', 'usSpinnerService', 'User', 'MenuItem',
    function ($scope, $location, $routeParams, $filter, $modal, usSpinnerService, User, MenuItem) {

    var start_block = function() {
        $.blockUI({baseZ: 2000, message: null});
        usSpinnerService.spin("spinner");
    };

    var stop_block = function() {
        usSpinnerService.stop("spinner");
        $.unblockUI();
    };

    var show_items = function() {
        var filter_shop = $scope.filters.shop;
        start_block();
        $scope.menu_items = MenuItem.queryByShopName({shop_name: filter_shop.name},
            function (response) { // 成功時
                stop_block();
            },
            function (response) {   // 失敗時
                alert("データが取得できませんでした。サインイン画面に戻ります。");
                stop_block();
                $scope.$dismiss();
                $location.path("/");
            });
    };

    $scope.menu_items = [];

    $scope.filters = {};
    $scope.filters.shop = {id: '@none', name: '選択してください'};

    $scope.select_shops = function() {
        var modalInstance = $modal.open({
            templateUrl: "/views/admin/daily-menu/select-shop",
            scope: $scope,
            controller: "DailyMenuSelectShopController"
        });

        modalInstance.result.then(function (selectedItem) {
            $scope.filters.shop = selectedItem;
            show_items();
        }, function () {
            console.log('Modal dismissed at: ' + new Date());
        });
    };

    $scope.edit_item = function(menu_item) {
        $scope.menu_item = menu_item;
        var modalInstance = $modal.open({
            templateUrl: "/views/admin/menu-item/edit",
            scope: $scope,
            controller: "MenuItemEditController",
            backdrop: "static"
        });
    };

    $scope.add_item = function() {
        $scope.menu_item = new MenuItem();
        var modalInstance = $modal.open({
            templateUrl: "/views/admin/menu-item/edit",
            scope: $scope,
            controller: "MenuItemEditController",
            backdrop: "static"
        });

        modalInstance.result.then(function (){
            $scope.menu_items.push($scope.menu_item);
        });
    };

    $scope.render_filter_shop = function() {
        return $scope.filters.shop.name;
    };

}]);