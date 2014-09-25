
angular.module('MyControllers')
.controller('DailyMenuNewController',
    ['$scope', '$location', '$routeParams', '$filter', 'User', 'MenuItem', 'DailyMenu',
    function ($scope, $location, $routeParams, $filter, User, MenuItem, DailyMenu) {

    // 5個ずつに分ける
    var groupingItems  = function() {
        var count_per_row = 5;
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

    // 変更を反映する
    var applyChanges = function(current_menu) {
        // 選択されていない場合はメニューを削除する
        if (current_menu.detail_items.length === 0) {
            if (current_menu.id !== undefined) {
                DailyMenu.remove({id: current_menu.id});
                current_menu.id = undefined;
            }
            return;
        }

        var menu = {id: current_menu.id, menu_date: current_menu.menu_date.unix() * 1000, status: "open", detail_items:[]};
        angular.forEach(current_menu.detail_items, function(item) {
            menu.detail_items.push(item);
        });

        if (menu.id === undefined) {
            DailyMenu.create({}, menu, function (saved) { current_menu.id = saved.id; });
        } else {
            DailyMenu.update({}, menu, function (saved) { current_menu.id = saved.id; });
        }
    };
    var lazyApplyChanges = _.debounce(applyChanges, 500);

    // メニューのItemを取得する
    $scope.menu_items = MenuItem.query({},
        function (response) { // 成功時
            // 表示のために5個ずつグルーピングする
            groupingItems();
        },
        function (response) {   // 失敗時
            alert("データが取得できませんでした。サインイン画面に戻ります。");
            $location.path("/");
        }
    );

    // 日付を変更する
    var changeMenuDate = function(target) {
        $scope.menu_date_begin = moment(target).startOf('week').add("days", 1); // startOfは日曜が取れるので月曜にシフト
        $scope.menu_date_end = moment($scope.menu_date_begin).add("days", 5);
        $scope.current_daily_menu = new DailyMenu();

        var param_menu_date = $scope.menu_date_begin.format("YYYY/MM/DD") + "-" + $scope.menu_date_end.format("YYYY/MM/DD");
        DailyMenu.query({"menu_date": param_menu_date},
            function (response) {
                $scope.daily_menus = [];
                for(var i=0; i<5; i++) {
                    var currentDate = moment($scope.menu_date_begin).add("days", i);
                    var menuIndex = DailyMenu.findByMenuDate(response, currentDate);

                    var menu = null;
                    if (menuIndex === -1) {
                        menu = new DailyMenu({ menu_date: currentDate, detail_items: [] });
                    } else {
                        menu = response[menuIndex];
                    }

                    $scope.daily_menus.push(menu);
                }
                $scope.current_daily_menu = $scope.daily_menus[0];
            },
            function (response) {
                alert("データが取得できませんでした。サインイン画面に戻ります。");
                $location.path("/");
            });
    };

    // カレンダーの初期化
    $("#datetimepicker").datetimepicker({pickTime: false, daysOfWeekDisabled: [0,6]});
    $("#datetimepicker").on("dp.change",function (e) {
        changeMenuDate(moment(e.date));
    });

    // 当日を起点としてメニューを表示する
    changeMenuDate(moment());

    //---- ヘルパ
    // 日付を選択しているか?
    $scope.is_day_selected = function(day) {
        if ($scope.current_daily_menu === undefined) {
            return false;
        }
        return ($scope.current_daily_menu.menu_date === day);
    };

    // 項目を選択しているか?
    $scope.is_item_selected = function(menu_item) {
        if ($scope.current_daily_menu === undefined) {
            return false;
        }
        return ($scope.current_daily_menu.findMenuItem(menu_item) >= 0);
    };

    // 項目の1週間の選択状態をHTMLにして出力
    $scope.render_item_select_status = function(menu_item) {

        if ($scope.daily_menus === undefined || $scope.current_daily_menu === undefined) {
            return "";
        }

        var status = "";

        angular.forEach($scope.daily_menus, function(daily_menu){
            var index = daily_menu.findMenuItem(menu_item);
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
        // 必ず保存する
        applyChanges($scope.current_daily_menu);

        $scope.current_daily_menu = daily_menu;
    };

    // 項目を選択する
    $scope.choose_item = function(menu_item) {
        var index = $scope.current_daily_menu.findMenuItem(menu_item);
        if ( index >= 0 ) {
            $scope.current_daily_menu.detail_items.splice(index, 1);
        } else {
            $scope.current_daily_menu.detail_items.push({menu_item: menu_item});
        }

        // 選択のたびに保存を行う
        lazyApplyChanges($scope.current_daily_menu);
    };

    // カレンダーを表示する
    $scope.show_calendar = function() {
        $("#datetimepicker").data("DateTimePicker").show();
    };
}]);
