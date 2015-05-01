(function(){

    angular.module('MyControllers')
        .controller('DailyMenuNewController', DailyMenuNewController);

    DailyMenuNewController.$inject = ['$location', '$filter', '$modal', 'dialogs', 'MenuItem', 'DailyMenu', 'DailyOrder', 'Assets'];

    function DailyMenuNewController($location, $filter, $modal, dialogs, MenuItem, DailyMenu, DailyOrder, Assets) {

        var vm = this;

        var maxNumOfBento = 9;
        var maxNumOfSide = 8;

        var compactSelectedItems = function () {
            var compacted = [];

            var pushItem = function (item) {
                if (item !== emptyItem) {
                    compacted.push(item);
                }
            };

            angular.forEach(vm.selectedItems.bento, pushItem);
            angular.forEach(vm.selectedItems.side, pushItem);

            return compacted;
        };

        // 変更を反映する
        var applyChanges = function (currentMenu) {

            var backup = {
                dailyMenu: angular.copy(currentMenu)
            };

            var errorHandler = function (result) {
                console.log(result);

                var errorDialog = null;
                switch (result.status) {
                    case 422:
                        var errorDetails = "";
                        angular.forEach(result.data.errors, function (value, key) {
                            errorDetails += key + " => " + value;
                        });
                        errorDialog = dialogs.error("データ登録・更新失敗", errorDetails);
                        break;

                    case 404:
                        errorDialog = dialogs.error("データ登録・更新失敗", result.data.message);
                        break;

                    default:
                        var messages = [
                            "処理中にエラーが発生しました",
                            "画面をリロードした後、再度操作を行ってみてください",
                            "問題が解消しない場合は管理者に連絡してください",
                            "",
                            "サーバ側のメッセージ: " + result.data.message
                        ];
                        errorDialog = dialogs.error("データ登録・更新失敗", messages.join("<br>"));
                        break;
                }

                errorDialog.result["finally"](function (config) {
                    currentMenu = backup.dailyMenu;
                    setCurrent(currentMenu);
                });
            };

            var selectedItems = compactSelectedItems();

            currentMenu.detailItems = [];
            angular.forEach(selectedItems, function (item) {
                currentMenu.detailItems.push({menuItem: item});
            });

            // 選択されていない場合はメニューを削除する
            if (selectedItems.length === 0) {
                console.log("#applyChanges selected items are empty.");
                console.log("#applyChanges menu.id:" + currentMenu.id);

                if (currentMenu.id !== undefined) {
                    DailyMenu.remove({id: currentMenu.id}, function (saved) {/*何もしない*/
                    }, errorHandler);
                    currentMenu.id = undefined;
                }
                return;
            }

            var menu = {
                id: currentMenu.id,
                menuDate: app.my.helpers.formatTimestamp(currentMenu.menuDate),
                status: currentMenu.status,
                detailItems: []
            };
            angular.forEach(currentMenu.detailItems, function (item) {
                menu.detailItems.push(item);
            });

            console.log("#applyChanges size:" + menu.detailItems.length);

            if (menu.id === undefined) {
                DailyMenu.create({}, menu, function (saved) {
                    currentMenu.id = saved.id;
                }, errorHandler);
            } else {
                DailyMenu.update({}, menu, function (saved) {
                    currentMenu.id = saved.id;
                }, errorHandler);
            }
        };
        var lazyApplyChanges = _.debounce(applyChanges, 500);

        // 日付を変更する
        var changeMenuDate = function (target) {
            target = moment(target.format('YYYY-MM-DDT00:00:00.000Z'));
            vm.menuDateBegin = moment(target).startOf('week').add("days", 1); // startOfは日曜が取れるので月曜にシフト
            vm.menuDateEnd = moment(vm.menuDateBegin).add("days", 4);
            vm.currentDailyMenu = new DailyMenu();

            var params = {
                "from": app.my.helpers.formatTimestamp(vm.menuDateBegin),
                "to": app.my.helpers.formatTimestamp(vm.menuDateEnd)
            };

            DailyMenu.query(params,
                function (response) {
                    console.log("#changeMenuDate DailyMenu size:" + response.length);

                    console.log("#changeMenuDate responses");
                    angular.forEach(response, function (item) {
                        console.log(app.my.helpers.formatTimestamp(item.menuDate));
                    });

                    vm.dailyMenus = [];
                    for (var i = 0; i < 5; i++) {
                        var currentDate = moment(vm.menuDateBegin).add("days", i);
                        console.log(app.my.helpers.formatTimestamp(currentDate));
                        var menuIndex = DailyMenu.findIndexByMenuDate(response, currentDate);

                        var menu = null;
                        if (menuIndex === -1) {
                            menu = new DailyMenu({menuDate: currentDate, status: "prepared", detailItems: []});
                        } else {
                            menu = response[menuIndex];
                            menu.detailItems = $filter('orderBy')(menu.detailItems, ['menuItem.shopName', 'menuItem.name']);
                        }

                        vm.dailyMenus.push(menu);
                    }

                    console.log("#changeMenuDate target: ");
                    console.log(target);
                    setCurrent($filter('getByMenuDate')(vm.dailyMenus, target));
                },
                function (response) {
                    alert("データが取得できませんでした。サインイン画面に戻ります。");
                    $location.path("/");
                });
        };

        var emptyItem = {id: undefined, name: "商品が選択されていません"};

        vm.selectedItems = {};

        var resetSelectedItems = function () {
            vm.selectedItems.bento = [];
            vm.selectedItems.side = [];

            for (i = 0; i < maxNumOfBento; i++) {
                vm.selectedItems.bento.push(emptyItem);
            }

            for (i = 0; i < maxNumOfSide; i++) {
                vm.selectedItems.side.push(emptyItem);
            }
        };

        var deployToSelectedItems = function (dailyMenu) {
            resetSelectedItems();

            console.log("#deployToSelectedItems detailItems:" + dailyMenu.detailItems.length);

            angular.forEach($filter('filter')(dailyMenu.detailItems, {menuItem: {category: 'bento'}}), function (item, key) {
                vm.selectedItems[item.menuItem.category][key] = item.menuItem;
            });

            angular.forEach($filter('filter')(dailyMenu.detailItems, {menuItem: {category: 'side'}}), function (item, key) {
                vm.selectedItems[item.menuItem.category][key] = item.menuItem;
            });
        };

        var setCurrent = function (dailyMenu) {
            console.log("#setCurrent");
            console.log(dailyMenu);
            vm.currentDailyMenu = dailyMenu;
            deployToSelectedItems(dailyMenu);
        };

        var setUp = function () {
            // カレンダーの初期化
            $("#datetimepicker").datetimepicker({
                language: 'ja',
                pickTime: false,
                daysOfWeekDisabled: [0, 6],
                useCurrent: false
            });
            $("#datetimepicker").on("dp.change", function (e) { // カレンダーで日付を変更した場合
                changeMenuDate(moment(e.date));
            });

            // 選択されている商品の初期化
            resetSelectedItems();

            // 来週の月曜日を起点としてメニューを表示する
            changeMenuDate(moment().add(1, "weeks").startOf('week').add(1, "days")); // startOfは日曜が取れるので月曜にシフト
        };

        //---- イベントハンドラ
        // カレンダーを表示する
        vm.showCalendar = function () {
            $("#datetimepicker").data("DateTimePicker").show();
        };

        // 日付を選択する
        vm.chooseDay = function (dailyMenu) {
            // 必ず保存する
            applyChanges(vm.currentDailyMenu);

            setCurrent(dailyMenu);
        };

        vm.changeMenuStatus = function (status) {
            vm.currentDailyMenu.status = status;
            lazyApplyChanges(vm.currentDailyMenu);
        };

        vm.selectItem = function (category, index) {
            var modalInstance = $modal.open({
                templateUrl: Assets.versioned("/views/admin/daily-menu/select-item"),
                controller: "DailyMenuSelectItemController",
                controllerAs: "vm",
                size: 'lg',
                resolve: {
                    category: function () {
                        return category;
                    },
                    selectedItems: function () {
                        return vm.selectedItems[category];
                    },
                    currentItem: function () {
                        return vm.selectedItems[category][index];
                    }
                }
            });

            modalInstance.result.then(function (selectedItem) {
                vm.selectedItems[category][index] = selectedItem;
                applyChanges(vm.currentDailyMenu);
            }, function () {
                console.log('Modal dismissed at: ' + new Date());
            });
        };

        vm.resetItem = function (category, index) {
            vm.selectedItems[category][index] = emptyItem;
            applyChanges(vm.currentDailyMenu);
        };


        vm.editOrder = function (dailyMenu) {
            var modalInstance = $modal.open({
                templateUrl: Assets.versioned("/views/admin/daily-order/edit"),
                controller: "DailyOrderEditController",
                size: 'lg',
                resolve: {
                    dailyMenu: function () {
                        return vm.currentDailyMenu;
                    }
                }
            });
        };

        vm.getCurrentMenuStatus = function () {
            return vm.currentDailyMenu.status;
        };

        var itemIsSelected = function (item) {
            return (item !== emptyItem);
        };

        vm.itemIsSelected = function (item) {
            return itemIsSelected(item);
        };

        // 日付を選択しているか?
        vm.isDaySelected = function (day) {
            if (vm.currentDailyMenu === undefined) {
                return false;
            }
            return (vm.currentDailyMenu.menuDate.valueOf() === day.valueOf());
        };

        vm.classForMenuItem = function (item) {
            return {
                'empty': !itemIsSelected(item),
                'selected': itemIsSelected(item)
            };
        };

        // 画像を表示するHTMLを出力
        vm.renderImage = function (menuItem) {
            var imgFile = "no-image.png";
            if (menuItem.itemImagePath !== undefined && menuItem.itemImagePath !== null) {
                imgFile = menuItem.itemImagePath;
            }
            return "<img src=\"/uc-assets/images/menu-items/" + imgFile + "\" alt=\"...\">";
        };

        setUp();
    }

})();
