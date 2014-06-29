
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

    $scope.showCreateOrderMenu = function () {
        $location.path("/admin/daily-menus/new");
    };

}]);
