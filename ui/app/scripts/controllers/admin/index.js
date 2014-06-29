
angular.module('MyControllers')
.controller('AdminIndexController',
    ['$scope', '$location', '$filter', 'User', 'DailyMenu', 'DailyOrder',
    function ($scope, $location, $filter, User, DailyMenu, DailyOrder) {

    $scope.daily_menus = DailyMenu.query({},
        function (response) { // 成功時
        // 何もしない
        },
        function (response) {   // 失敗時
            alert("メニューのデータが取得できませんでした。サインイン画面に戻ります。");
            $location.path("/");
        }
    );

    $scope.showChecklist = function (daily_menu) {
        $location.path("/admin/checklist/menu_date/" + daily_menu.menu_date.format('YYYY-MM-DD'));
    };

    $scope.showAdminIndex = function () {
        $location.path("/admin/index");
    };

    $scope.showCreateOrderMenu = function () {
        $location.path("/admin/daily-menus/new");
    };
}]);

