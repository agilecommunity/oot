
angular.module('MyControllers')
.controller('DailyMenuNewController',
    ['$scope', '$location', '$routeParams', '$filter', '$modal', 'User', 'MenuItem', 'DailyMenu', 'DailyOrder',
    function ($scope, $location, $routeParams, $filter, $modal, User, MenuItem, DailyMenu, DailyOrder) {

    var maxNumOfBento = 9;
    var maxNumOfSide = 8;

    var compactSelectedItems = function() {
        var compacted = [];

        var pushItem = function(item) {
            if (item !== emptyItem) {
                compacted.push(item);
            }
        };

        angular.forEach($scope.selectedItems.bento, pushItem);
        angular.forEach($scope.selectedItems.side, pushItem);

        return compacted;
    };

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

        var menu = {
            id: currentMenu.id,
            menuDate: app.my.helpers.formatTimestamp(currentMenu.menuDate),
            status: currentMenu.status,
            detailItems:[]
        };
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
        target = moment(target.format('YYYY-MM-DDT00:00:00.000Z'));
        $scope.menuDateBegin = moment(target).startOf('week').add("days", 1); // startOfは日曜が取れるので月曜にシフト
        $scope.menuDateEnd = moment($scope.menuDateBegin).add("days", 4);
        $scope.currentDailyMenu = new DailyMenu();

        var params = {
            "from": app.my.helpers.formatTimestamp($scope.menuDateBegin),
            "to": app.my.helpers.formatTimestamp($scope.menuDateEnd)
        };

        DailyMenu.query(params,
            function (response) {
                console.log("#changeMenuDate DailyMenu size:" + response.length);

                console.log("#changeMenuDate responses");
                angular.forEach(response, function(item){
                    console.log(app.my.helpers.formatTimestamp(item.menuDate));
                });

                $scope.dailyMenus = [];
                for(var i=0; i<5; i++) {
                    var currentDate = moment($scope.menuDateBegin).add("days", i);
                    console.log(app.my.helpers.formatTimestamp(currentDate));
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

                console.log("#changeMenuDate target: ");
                console.log(target);
                setCurrent($filter('getByMenuDate')($scope.dailyMenus, target));
            },
            function (response) {
                alert("データが取得できませんでした。サインイン画面に戻ります。");
                $location.path("/");
            });
    };

    var emptyItem = {id: undefined, name: "商品が選択されていません"};

    $scope.selectedItems = {};

    var resetSelectedItems = function() {
        $scope.selectedItems.bento = [];
        $scope.selectedItems.side = [];

        for(i=0; i<maxNumOfBento; i++) {
            $scope.selectedItems.bento.push(emptyItem);
        }

        for(i=0; i<maxNumOfSide; i++) {
            $scope.selectedItems.side.push(emptyItem);
        }
    };

    var deployToSelectedItems = function(dailyMenu) {
        resetSelectedItems();

        console.log("#deployToSelectedItems detailItems:" + dailyMenu.detailItems.length);

        angular.forEach(dailyMenu.detailItems, function(item){
            $scope.selectedItems[item.menuItem.category].unshift(item.menuItem);
            $scope.selectedItems[item.menuItem.category].pop();
        });
    };

    var setCurrent = function(dailyMenu) {
        console.log("#setCurrent");
        console.log(dailyMenu);
        $scope.currentDailyMenu = dailyMenu;
        deployToSelectedItems(dailyMenu);
    };

    var setUp = function() {
        // カレンダーの初期化
        $("#datetimepicker").datetimepicker({language: 'ja', pickTime: false, daysOfWeekDisabled: [0,6], useCurrent: false});
        $("#datetimepicker").on("dp.change",function (e) { // カレンダーで日付を変更した場合
            changeMenuDate(moment(e.date));
        });

        // 選択されている商品の初期化
        resetSelectedItems();

        // 来週の月曜日を起点としてメニューを表示する
        changeMenuDate(moment().add(1, "weeks").startOf('week').add(1, "days")); // startOfは日曜が取れるので月曜にシフト
    };

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

    $scope.selectItem = function(category, index) {
        var modalInstance = $modal.open({
            templateUrl: "/views/admin/daily-menu/select-item",
            controller: "DailyMenuSelectItemController",
            size: 'lg',
            resolve: {
                category: function() {
                    return category;
                },
                selectedItems: function() {
                    return $scope.selectedItems[category];
                },
                currentItem: function() {
                    return $scope.selectedItems[category][index];
                }
            }
        });

        modalInstance.result.then(function (selectedItem) {
            $scope.selectedItems[category][index] = selectedItem;
            applyChanges($scope.currentDailyMenu);
        }, function () {
            console.log('Modal dismissed at: ' + new Date());
        });
    };

    $scope.resetItem = function(category, index) {
        $scope.selectedItems[category][index] = emptyItem;
        applyChanges($scope.currentDailyMenu);
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

    $scope.getCurrentMenuStatus = function() {
        return $scope.currentDailyMenu.status;
    };

    var itemIsSelected = function(item) {
        return (item !== emptyItem);
    };

    $scope.itemIsSelected = function(item) {
        return itemIsSelected(item);
    };

    // 日付を選択しているか?
    $scope.isDaySelected = function(day) {
        if ($scope.currentDailyMenu === undefined) {
            return false;
        }
        return ($scope.currentDailyMenu.menuDate === day);
    };

    $scope.classForMenuItem = function(item) {
        return {
            'empty': !itemIsSelected(item),
            'selected': itemIsSelected(item)
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

    setUp();

}]);
