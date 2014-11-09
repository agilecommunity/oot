angular.module('MyControllers')
    .controller('DailyMenuSelectItemController',
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

    var grouping_items  = function() {
        var count_per_row = 4;
        $scope.group_menu_items = [];
        var group = [];
        for ( var i=0 ; i < $scope.menu_items.length ; i++ ) {
            group.push($scope.menu_items[i]);

            if ((i+1) % count_per_row === 0 || (i+1) === $scope.menu_items.length) {
                $scope.group_menu_items.push(group);
                group = [];
            }
        }
    };

    var show_items = function() {
        var filter_shop = $scope.filters.shop;
        start_block();
        $scope.menu_items = MenuItem.queryByShopName({shop_name: filter_shop.name},
            function (response) { // 成功時
                // 表示のために5個ずつグルーピングする
                grouping_items();
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
    $scope.group_menu_items = [];

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

    $scope.is_empty = function() {
        return $scope.group_menu_items.length === 0;
    };

    $scope.select_this = function(item) {
        $scope.$close(item);
    };

    $scope.render_filter_shop = function() {
        return $scope.filters.shop.name;
    };

    // 画像を表示するHTMLを出力
    $scope.render_image = function(menu_item) {
        var imgFile = "no-image.png";
        if (menu_item.item_image_path !== undefined && menu_item.item_image_path !== null) {
            imgFile = menu_item.item_image_path;
        }
        return "<img src=\"/uc-assets/images/menu-items/" + imgFile + "\" alt=\"...\" width=\"100px\" height=\"100px\">";
    };

}]);