
angular.module('MyControllers')
.controller('DailyMenuNewController',
    ['$scope', '$location', '$routeParams', '$filter', '$modal', 'User', 'MenuItem', 'DailyMenu', 'DailyOrder',
    function ($scope, $location, $routeParams, $filter, $modal, User, MenuItem, DailyMenu, DailyOrder) {

    // 変更を反映する
    var applyChanges = function(currentMenu) {

        var selectedItems = compactSelectedItems();

        currentMenu.detailItems = [];
        angular.forEach(selectedItems, function(item) {
            currentMenu.detailItems.push({menuItem: item});
        });

        // 選択されていない場合はメニューを削除する
        if (selectedItems.length === 0) {
            console.log("#applyChanges selected items are empty.");
            console.log("#applyChanges menu.id:" + currentMenu.id);

            if (currentMenu.id !== undefined) {
                DailyMenu.remove({id: currentMenu.id});
                currentMenu.id = undefined;
            }
            return;
        }

        var menu = {id: currentMenu.id, menuDate: currentMenu.menuDate.format("YYYY-MM-DD"), status: currentMenu.status, detailItems:[]};
        angular.forEach(currentMenu.detailItems, function(item) {
            menu.detailItems.push(item);
        });

        console.log("#applyChanges size:" + menu.detailItems.length);

        if (menu.id === undefined) {
            DailyMenu.create({}, menu, function (saved) { currentMenu.id = saved.id; });
        } else {
            DailyMenu.update({}, menu, function (saved) { currentMenu.id = saved.id; });
        }
    };
    var lazyApplyChanges = _.debounce(applyChanges, 500);

    // 日付を変更する
    var changeMenuDate = function(target) {
        $scope.menuDateBegin = target.startOf('week').add("days", 1); // startOfは日曜が取れるので月曜にシフト
        $scope.menuDateEnd = moment.utc($scope.menuDateBegin).add("days", 4);
        $scope.currentDailyMenu = new DailyMenu();

        var params = {
            "from": $scope.menuDateBegin.format("YYYY-MM-DD"),
            "to": $scope.menuDateEnd.format("YYYY-MM-DD")
        };

        DailyMenu.query(params,
            function (response) {
                console.log("#changeMenuDate DailyMenu size:" + response.length);

                console.log("#changeMenuDate responses");
                angular.forEach(response, function(item){
                    console.log(item.menuDate.format("YYYY/MM/DD HH:mm:ss"));
                });

                $scope.dailyMenus = [];
                for(var i=0; i<5; i++) {
                    var currentDate = moment.utc($scope.menuDateBegin).add("days", i);
                    console.log(currentDate.format("YYYY/MM/DD HH:mm:ss"));
                    var menuIndex = DailyMenu.findByMenuDate(response, currentDate);

                    var menu = null;
                    if (menuIndex === -1) {
                        menu = new DailyMenu({ menuDate: currentDate, status: "prepared", detailItems: [] });
                    } else {
                        menu = response[menuIndex];
                        menu.detailItems = $filter('orderBy')(menu.detailItems, ['menuItem.shopName', 'menuItem.name']);
                    }

                    $scope.dailyMenus.push(menu);
                }
                setCurrent($scope.dailyMenus[0]);
            },
            function (response) {
                alert("データが取得できませんでした。サインイン画面に戻ります。");
                $location.path("/");
            });
    };

    var emptyItem = {id: undefined, name: "商品が選択されていません"};
    var itemMaxNum = 12;
    var resetSelectedItems = function() {
        $scope.selectedItems = [];
        for(var i=0; i<itemMaxNum; i++) {
            $scope.selectedItems[i] = emptyItem;
        }
    };

    var compactSelectedItems = function() {
        var compacted = [];
        angular.forEach($scope.selectedItems, function(item){
            if (item !== emptyItem) {
                compacted.push(item);
            }
        });
        return compacted;
    };

    var deployToSelectedItems = function(dailyMenu) {
        resetSelectedItems();

        console.log("#deployToSelectedItems detailItems:" + dailyMenu.detailItems.length);

        var count = 0;
        angular.forEach(dailyMenu.detailItems, function(item){
            $scope.selectedItems[count] = item.menuItem;
            count++;
        });
    };

    var setCurrent = function(dailyMenu) {
        console.log("#setCurrent");
        console.log(dailyMenu);
        $scope.currentDailyMenu = dailyMenu;
        deployToSelectedItems(dailyMenu);
    };

    // カレンダーの初期化
    $("#datetimepicker").datetimepicker({pickTime: false, daysOfWeekDisabled: [0,6]});
    $("#datetimepicker").on("dp.change",function (e) { // カレンダーで日付を変更した場合
        changeMenuDate(moment.utc(e.date));
    });

    // 選択されている商品の初期化
    resetSelectedItems();

    // 当日を起点としてメニューを表示する
    changeMenuDate(moment.utc());

    //---- イベントハンドラ
    // カレンダーを表示する
    $scope.showCalendar = function() {
        $("#datetimepicker").data("DateTimePicker").show();
    };

    // 日付を選択する
    $scope.chooseDay = function(dailyMenu) {
        // 必ず保存する
        applyChanges($scope.currentDailyMenu);

        setCurrent(dailyMenu);
    };

    $scope.changeMenuStatus = function(status) {
        $scope.currentDailyMenu.status = status;
        lazyApplyChanges($scope.currentDailyMenu);
    };

    $scope.selectItem = function(itemIndex) {
        var modalInstance = $modal.open({
            templateUrl: "/views/admin/daily-menu/select-item",
            controller: "DailyMenuSelectItemController",
            scope: $scope,
            size: 'lg'
        });

        modalInstance.result.then(function (selectedItem) {
            $scope.selectedItems[itemIndex] = selectedItem;
            applyChanges($scope.currentDailyMenu);
        }, function () {
            console.log('Modal dismissed at: ' + new Date());
        });
    };

    $scope.editOrder = function(dailyMenu) {
        var modalInstance = $modal.open({
            templateUrl: "/views/admin/daily-order/edit",
            controller: "DailyOrderEditController",
            size: 'lg',
            resolve: {
                dailyMenu: function() {
                    return $scope.currentDailyMenu;
                }
            }
        });
    };

    $scope.resetItem = function(index) {
        $scope.selectedItems[index] = emptyItem;
        applyChanges($scope.currentDailyMenu);
    };

    var itemIsSelected = function(index) {
        return ($scope.selectedItems[index] !== emptyItem);
    };

    $scope.getCurrentMenuStatus = function() {
        return $scope.currentDailyMenu.status;
    };

    $scope.itemIsSelected = function(index) {
        return itemIsSelected(index);
    };

    // 日付を選択しているか?
    $scope.isDaySelected = function(day) {
        if ($scope.currentDailyMenu === undefined) {
            return false;
        }
        return ($scope.currentDailyMenu.menuDate === day);
    };

    $scope.classForTile = function(index) {
        return {
            'empty': !itemIsSelected(index),
            'selected': itemIsSelected(index)
        };
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
