
angular.module('MyControllers')
.controller('AdminIndexController',
    ['$scope', '$location', '$filter', 'User', 'DailyMenu', 'DailyOrder',
    function ($scope, $location, $filter, User, DailyMenu, DailyOrder) {

    $scope.dailyMenus = DailyMenu.query({},
        function (response) { // 成功時
        // 何もしない
        },
        function (response) {   // 失敗時
            alert("メニューのデータが取得できませんでした。サインイン画面に戻ります。");
            $location.path("/");
        }
    );

    $scope.showOrderAggregates = function (dailyMenu) {
        $location.path("/admin/order-aggregates/order-date/" + dailyMenu.menuDate.format('YYYY-MM-DD'));
    };

    $scope.showChecklist = function (dailyMenu) {
        $location.path("/admin/checklist/menu-date/" + dailyMenu.menuDate.format('YYYY-MM-DD'));
    };

}]);

