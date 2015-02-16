angular.module('MyControllers')
    .controller('DailyMenuSelectItemController',
    ['$scope', '$location', '$routeParams', '$filter', '$modal', '_s', 'usSpinnerService', 'User', 'MenuItem', 'category', 'selectedItems', 'currentItem',
    function ($scope, $location, $routeParams, $filter, $modal, _s, usSpinnerService, User, MenuItem, category, selectedItems, currentItem) {

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
        var filterShop = $scope.filters.shop;
        startBlock();
        $scope.menuItems = MenuItem.queryByShopName({shopName: filterShop.name, status: 'valid'},
            function (response) { // 成功時
                // 表示のために5個ずつグルーピングする
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

    $scope.isEmpty = function() {
        return $scope.groupMenuItems.length === 0;
    };

    $scope.isSelected = function(item) {
        var selected = ($filter('getById')(selectedItems, item.id) !== null);
        return (selected && item.id !== currentItem.id);
    };

    $scope.selectThis = function(item) {
        if ($scope.isSelected(item)) {
            return;
        }
        $scope.$close(item);
    };

    $scope.renderFilterShop = function() {
        return $scope.filters.shop.name;
    };

    // 画像を表示するHTMLを出力
    $scope.renderImage = function(menuItem) {
        var imgFile = "no-image.png";
        if (menuItem.itemImagePath !== undefined && menuItem.itemImagePath !== null) {
            imgFile = menuItem.itemImagePath;
        }
        return "<img src=\"/uc-assets/images/menu-items/" + imgFile + "\" alt=\"...\">";
    };

}]);