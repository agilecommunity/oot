
angular.module('MyControllers')
.controller('DailyMenuNewController',
    ['$scope', '$location', '$routeParams', '$filter', '$modal', 'User', 'MenuItem', 'DailyMenu',
    function ($scope, $location, $routeParams, $filter, $modal, User, MenuItem, DailyMenu) {

    // 変更を反映する
    var apply_changes = function(current_menu) {

        var selected_items = compact_selected_items();

        current_menu.detail_items = [];
        angular.forEach(selected_items, function(item) {
            current_menu.detail_items.push({menu_item: item});
        });

        // 選択されていない場合はメニューを削除する
        if (selected_items.length === 0) {
            console.log("#applyChanges selected items are empty.");
            console.log("#applyChanges menu.id:" + current_menu.id);

            if (current_menu.id !== undefined) {
                DailyMenu.remove({id: current_menu.id});
                current_menu.id = undefined;
            }
            return;
        }

        var menu = {id: current_menu.id, menu_date: current_menu.menu_date.format("YYYY-MM-DD"), status: current_menu.status, detail_items:[]};
        angular.forEach(current_menu.detail_items, function(item) {
            menu.detail_items.push(item);
        });

        console.log("#applyChanges size:" + menu.detail_items.length);

        if (menu.id === undefined) {
            DailyMenu.create({}, menu, function (saved) { current_menu.id = saved.id; });
        } else {
            DailyMenu.update({}, menu, function (saved) { current_menu.id = saved.id; });
        }
    };
    var lazy_apply_changes = _.debounce(apply_changes, 500);

    // 日付を変更する
    var chage_menu_date = function(target) {
        $scope.menu_date_begin = target.startOf('week').add("days", 1); // startOfは日曜が取れるので月曜にシフト
        $scope.menu_date_end = moment.utc($scope.menu_date_begin).add("days", 4);
        $scope.current_daily_menu = new DailyMenu();

        var params = {
            "from": $scope.menu_date_begin.format("YYYY-MM-DD"),
            "to": $scope.menu_date_end.format("YYYY-MM-DD")
        };

        DailyMenu.query(params,
            function (response) {
                console.log("#changeMenuDate DailyMenu size:" + response.length);

                console.log("#changeMenuDate responses");
                angular.forEach(response, function(item){
                    console.log(item.menu_date.format("YYYY/MM/DD HH:mm:ss"));
                });

                $scope.daily_menus = [];
                for(var i=0; i<5; i++) {
                    var currentDate = moment.utc($scope.menu_date_begin).add("days", i);
                    console.log(currentDate.format("YYYY/MM/DD HH:mm:ss"));
                    var menuIndex = DailyMenu.findByMenuDate(response, currentDate);

                    var menu = null;
                    if (menuIndex === -1) {
                        menu = new DailyMenu({ menu_date: currentDate, status: "prepared", detail_items: [] });
                    } else {
                        menu = response[menuIndex];
                    }

                    $scope.daily_menus.push(menu);
                }
                set_current($scope.daily_menus[0]);
            },
            function (response) {
                alert("データが取得できませんでした。サインイン画面に戻ります。");
                $location.path("/");
            });
    };

    var empty_item = {id: undefined, name: "商品が選択されていません"};
    var item_max_num = 12;
    var reset_selected_items = function() {
        $scope.selected_items = [];
        for(var i=0; i<item_max_num; i++) {
            $scope.selected_items[i] = empty_item;
        }
    };

    var compact_selected_items = function() {
        var compacted = [];
        angular.forEach($scope.selected_items, function(item){
            if (item !== empty_item) {
                compacted.push(item);
            }
        });
        return compacted;
    };

    var deploy_to_selected_items = function(daily_menu) {
        reset_selected_items();

        console.log("#deploy_to_selected_items detail_items:" + daily_menu.detail_items.length);

        var count = 0;
        angular.forEach(daily_menu.detail_items, function(item){
            $scope.selected_items[count] = item.menu_item;
            count++;
        });
    };

    var set_current = function(daily_menu) {
        console.log("#set_current");
        console.log(daily_menu);
        $scope.current_daily_menu = daily_menu;
        deploy_to_selected_items(daily_menu);
    };

    // カレンダーの初期化
    $("#datetimepicker").datetimepicker({pickTime: false, daysOfWeekDisabled: [0,6]});
    $("#datetimepicker").on("dp.change",function (e) { // カレンダーで日付を変更した場合
        chage_menu_date(moment.utc(e.date));
    });

    // 選択されている商品の初期化
    reset_selected_items();

    // 当日を起点としてメニューを表示する
    chage_menu_date(moment.utc());

    //---- イベントハンドラ
    // カレンダーを表示する
    $scope.show_calendar = function() {
        $("#datetimepicker").data("DateTimePicker").show();
    };

    // 日付を選択する
    $scope.choose_day = function(daily_menu) {
        // 必ず保存する
        apply_changes($scope.current_daily_menu);

        set_current(daily_menu);
    };

    $scope.change_menu_status = function(status) {
        $scope.current_daily_menu.status = status;
        lazy_apply_changes($scope.current_daily_menu);
    };

    $scope.select_item = function(itemIndex) {
        var modalInstance = $modal.open({
            templateUrl: "/views/admin/daily-menu/select-item",
            controller: "DailyMenuSelectItemController",
            scope: $scope,
            size: 'lg'
        });

        modalInstance.result.then(function (selectedItem) {
            $scope.selected_items[itemIndex] = selectedItem;
            apply_changes($scope.current_daily_menu);
        }, function () {
            console.log('Modal dismissed at: ' + new Date());
        });
    };

    $scope.reset_item = function(index) {
        $scope.selected_items[index] = empty_item;
        apply_changes($scope.current_daily_menu);
    };

    //---- ヘルパ
    var item_is_selected = function(index) {
        return ($scope.selected_items[index] !== empty_item);
    };

    $scope.get_current_menu_status = function() {
        return $scope.current_daily_menu.status;
    };

    $scope.item_is_selected = function(index) {
        return item_is_selected(index);
    };

    // 日付を選択しているか?
    $scope.is_day_selected = function(day) {
        if ($scope.current_daily_menu === undefined) {
            return false;
        }
        return ($scope.current_daily_menu.menu_date === day);
    };

    $scope.class_for_tile = function(index) {
        return {
            'empty': !item_is_selected(index),
            'selected': item_is_selected(index)
        };
    };

    // 画像を表示するHTMLを出力
    $scope.render_image = function(menu_item) {
        var imgFile = "no-image.png";
        if (menu_item.item_image_path !== undefined && menu_item.item_image_path !== null) {
            imgFile = menu_item.item_image_path;
        }
        return "<img src=\"/uc-assets/images/menu-items/" + imgFile + "\" alt=\"...\">";
    };

}]);
