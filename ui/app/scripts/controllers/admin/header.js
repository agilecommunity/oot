
angular.module('MyControllers')
    .controller('AdminHeaderController',
    ['$scope', '$location', '$filter', 'User',
    function ($scope, $location, $filter, User, DailyMenu, DailyOrder) {

    //---- ヘルパ
    $scope.isActive = function(url) {
        return $location.path() === url;
    };

    //---- イベントハンドラ
    $scope.showAdminIndex = function () {
        $location.path("/admin/index");
    };

    $scope.showOrderMenuCreate = function () {
        $location.path("/admin/daily-menus/new");
    };

    $scope.showMenuItemsIndex = function () {
        $location.path("/admin/menu-items/index");
    };

    $scope.showMenuItemsImport = function () {
        $location.path("/admin/menu-items/import");
    };

    $scope.showMenuItemImagesImport = function () {
        $location.path("/admin/menu-item-images/import");
    };

}]);
