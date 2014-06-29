
angular.module('MyControllers')
.controller('DailyMenuNewController',
    ['$scope', '$location', '$routeParams', '$filter', 'User', 'DailyMenu', 'DailyOrder',
    function ($scope, $location, $routeParams, $filter, User, DailyMenu, DailyOrder) {

    $scope.menu_items = [];
    for ( var i=0 ; i < 200; i++ ) {
        var item = { shop_name: "会社" + i, name: "会社" + i + "-弁当" + i + "とてもながいお弁当の名前", price_on_order: 510 + i };
        $scope.menu_items.push(item);
    }

    // 5個ずつに分ける
    var count_per_row = 5;
    $scope.group_menu_items = [];
    var group = [];
    for ( i=0 ; i < $scope.menu_items.length ; i++ ) {
        group.push($scope.menu_items[i]);

        if ((i+1) % count_per_row === 0 || (i+1) === $scope.menu_items.length) {
            $scope.group_menu_items.push(group);
            group = [];
        }
    }

    $scope.daily_menus = [
        { menu_date: moment("2014/05/26"), detail_items: [] },
        { menu_date: moment("2014/05/27"), detail_items: [] },
        { menu_date: moment("2014/05/28"), detail_items: [] },
        { menu_date: moment("2014/05/29"), detail_items: [] },
        { menu_date: moment("2014/05/30"), detail_items: [] }
    ];

    $scope.current_daily_menu = $scope.daily_menus[0];

    //---- ヘルパ
    // 日付を選択しているか?
    $scope.is_day_selected = function(day) {
        return ($scope.current_daily_menu.menu_date === day);
    };

    // 項目を選択しているか?
    $scope.is_item_selected = function(menu_item) {
        return (jQuery.inArray(menu_item, $scope.current_daily_menu.detail_items) >= 0);
    };

    // 項目の1週間の選択状態をHTMLにして出力
    $scope.render_item_select_status = function(menu_item) {

        var status = "";

        angular.forEach($scope.daily_menus, function(daily_menu){
            var index = jQuery.inArray(menu_item, daily_menu.detail_items);
            if ( index >= 0 ) {
                var class_name = "text-muted";
                if ( daily_menu === $scope.current_daily_menu ) {
                    class_name = "text-primary";
                }
                status += "<span class=\"" + class_name + "\">●</span>";
            } else {
                status += "<span class=\"text-muted\">○</span>";
            }
        });

        return status;
    };

    //---- イベントハンドラ
    // 日付を選択する
    $scope.choose_day = function(daily_menu) {
        $scope.current_daily_menu = daily_menu;
    };

    // 項目を選択する
    $scope.choose_item = function(menu_item) {
        var index = jQuery.inArray(menu_item, $scope.current_daily_menu.detail_items);
        if ( index >= 0 ) {
            $scope.current_daily_menu.detail_items.splice(index, 1);
        } else {
            $scope.current_daily_menu.detail_items.push(menu_item);
        }
    };

    $scope.showAdminIndex = function () {
        $location.path("/admin/index");
    };

    $scope.showCreateOrderMenu = function () {
        $location.path("/admin/daily-menus/new");
    };

}]);
