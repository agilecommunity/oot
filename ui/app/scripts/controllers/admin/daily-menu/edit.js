(function(){

    // FIXME: UI.bootstrapがバージョンアップされてテンプレートの入れ替えができるようになったら書き換えること
    // DatePickerのテンプレートを再定義
    // ボタンにクラスを追加して、テストしやすくした
    angular.module("template/datepicker/day.html", []).run(["$templateCache", function($templateCache) {
        $templateCache.put("template/datepicker/day.html",
            "<table class=\"tbl-datepicker-days\" role=\"grid\" aria-labelledby=\"{{uniqueId}}-title\" aria-activedescendant=\"{{activeDateId}}\">\n" +
            "  <thead>\n" +
            "    <tr>\n" +
            "      <th><button type=\"button\" class=\"btn btn-default btn-sm pull-left btn-datepicker-prev-month\" ng-click=\"move(-1)\" tabindex=\"-1\"><i class=\"glyphicon glyphicon-chevron-left\"></i></button></th>\n" +
            "      <th colspan=\"{{5 + showWeeks}}\"><button id=\"{{uniqueId}}-title\" role=\"heading\" aria-live=\"assertive\" aria-atomic=\"true\" type=\"button\" class=\"btn btn-default btn-sm btn-datepicker-toggle\" ng-click=\"toggleMode()\" tabindex=\"-1\" style=\"width:100%;\"><strong>{{title}}</strong></button></th>\n" +
            "      <th><button type=\"button\" class=\"btn btn-default btn-sm pull-right btn-datepicker-next-month\" ng-click=\"move(1)\" tabindex=\"-1\"><i class=\"glyphicon glyphicon-chevron-right\"></i></button></th>\n" +
            "    </tr>\n" +
            "    <tr>\n" +
            "      <th ng-show=\"showWeeks\" class=\"text-center\"></th>\n" +
            "      <th ng-repeat=\"label in labels track by $index\" class=\"text-center\"><small aria-label=\"{{label.full}}\">{{label.abbr}}</small></th>\n" +
            "    </tr>\n" +
            "  </thead>\n" +
            "  <tbody>\n" +
            "    <tr ng-repeat=\"row in rows track by $index\">\n" +
            "      <td ng-show=\"showWeeks\" class=\"text-center h6\"><em>{{ weekNumbers[$index] }}</em></td>\n" +
            "      <td ng-repeat=\"dt in row track by dt.date\" class=\"text-center\" role=\"gridcell\" id=\"{{dt.uid}}\" aria-disabled=\"{{!!dt.disabled}}\">\n" +
            "        <button type=\"button\" style=\"width:100%;\" class=\"btn btn-default btn-sm\" ng-class=\"{'btn-info': dt.selected, active: isActive(dt)}\" ng-click=\"select(dt.date)\" ng-disabled=\"dt.disabled\" tabindex=\"-1\"><span ng-class=\"{'text-muted': dt.secondary, 'text-info': dt.current}\">{{dt.label}}</span></button>\n" +
            "      </td>\n" +
            "    </tr>\n" +
            "  </tbody>\n" +
            "</table>\n" +
            "");
    }]);

    // FIXME: UI.bootstrapがバージョンアップされてテンプレートの入れ替えができるようになったら書き換えること
    // DatePickerのテンプレートを再定義
    // 閉じるボタンを削除
    // クラスを追加し、テストしやすくした
    angular.module("template/datepicker/popup.html", []).run(["$templateCache", function($templateCache) {
        $templateCache.put("template/datepicker/popup.html",
            "<ul class=\"dropdown-menu dropdown-menu-datepicker\" ng-style=\"{display: (isOpen && 'block') || 'none', top: position.top+'px', left: position.left+'px'}\" ng-keydown=\"keydown($event)\">\n" +
            "	<li ng-transclude></li>\n" +
            "	<li ng-if=\"showButtonBar\" style=\"padding:10px 9px 2px\">\n" +
            "		<span class=\"btn-group\">\n" +
            "			<button type=\"button\" class=\"btn btn-sm btn-info\" ng-click=\"select('today')\">{{ getText('current') }}</button>\n" +
            "		</span>\n" +
            "	</li>\n" +
            "</ul>\n" +
            "");
    }]);

    var DayGroup = (function() {

        var maxNumOfBento = 9;
        var maxNumOfSide = 8;
        var emptyItem = {id: undefined, name: "商品が選択されていません"};

        var compactItems = function(selectedItems) {
            console.log(selectedItems);

            var compacted = [];

            var pushItem = function (item) {
                if (item !== emptyItem) {
                    compacted.push(item);
                }
            };

            angular.forEach(selectedItems.bento, pushItem);
            angular.forEach(selectedItems.side, pushItem);

            return compacted;
        };

        var saveMenu = function(menu, selectedItems) {
            menu.detailItems = [];
            angular.forEach(selectedItems, function (selectedItem) {
                menu.detailItems.push({menuItem: selectedItem});
            });

            if (menu.id === undefined || menu.id === null) {
                return menu.$create();
            } else {
                return menu.$update();
            }
        };

        var deleteMenu = function(menu) {
            return menu.$remove(function(saved){
                // 削除したらオブジェクトは初期化する
                menu.id = null;
                menu.status = "prepared";
            });
        };

        var syncItems = function(menu, selectedItems) {

            console.log("#syncToSelectedItems detailItems:" + menu.detailItems.length);

            angular.forEach(menu.filterDetailItems('bento'), function (item, index) {
                selectedItems[item.menuItem.category][index] = item.menuItem;
            });

            angular.forEach(menu.filterDetailItems('side'), function (item, index) {
                selectedItems[item.menuItem.category][index] = item.menuItem;
            });
        };


        function MyClass($filter, $q, day, menu) {
            this.$filter = $filter;
            this.$q = $q;
            this.day = day;
            this.menu = menu;
            this.resetItems();
            syncItems(menu, this.selectedItems);
        }

        MyClass.prototype.syncServer = function() {

            compactedItems = compactItems(this.selectedItems);

            if (compactedItems.length === 0) {
                if (this.menu.id) {
                    return deleteMenu(this.menu);
                } else {
                    return this.$q.when(this.menu);
                }
            }

            return saveMenu(this.menu, compactedItems);
        };

        MyClass.prototype.getItem = function(category, index) {
            return this.selectedItems[category][index];
        };

        MyClass.prototype.getItems = function(category) {
            return this.$filter("filter")(this.selectedItems[category], {category: category});
        };

        MyClass.prototype.itemIsSelected = function(category, index) {
            return (this.getItem(category, index) !== emptyItem);
        };

        MyClass.prototype.setItem = function(category, index, menuItem) {
            this.selectedItems[category][index] = menuItem;
        };

        MyClass.prototype.resetItem = function(category, index) {
            this.selectedItems[category][index] = emptyItem;
        };

        MyClass.prototype.resetItems = function () {

            this.selectedItems = {};
            this.selectedItems.bento = [];
            this.selectedItems.side = [];

            for (i = 0; i < maxNumOfBento; i++) {
                this.selectedItems.bento.push(emptyItem);
            }

            for (i = 0; i < maxNumOfSide; i++) {
                this.selectedItems.side.push(emptyItem);
            }
        };

        return MyClass;
    })();

    angular.module('MyControllers')
        .controller('DailyMenuEditController', DailyMenuEditController);

    DailyMenuEditController.$inject = ['$scope', '$location', '$filter', '$modal', '$q', 'MyDialogs', 'usSpinnerService', 'Assets', 'initialData'];

    function DailyMenuEditController($scope, $location, $filter, $modal, $q, MyDialogs, usSpinnerService, Assets, initialData) {

        var vm = this;

        var uiBlocker = {
            start: function() {
                $.blockUI({baseZ: 2000, message: null});
                usSpinnerService.spin("spinner");
            },
            stop: function() {
                usSpinnerService.stop("spinner");
                $.unblockUI();
            }
        };

        // 1週間のデータを作成する
        var createWeek = function(beginDay) {

            var days = [];
            for(var index=0; index<5; index++) {
                var currentDay = moment(beginDay).add(index, "days");

                days.push({
                    day: currentDay
                });
            }

            return {
                beginDay: beginDay,
                endDay: _.last(days).day,
                days: days
            };
        };

        var toUTCDate = function(day) {
            return moment(day).utc().add(9, "hours").toDate();
        };

        // エラーダイアログを表示する
        var showErrorDialog = function (result) {
            var errorDialog = null;
            switch (result.status) {
                case 422:
                    var errorDetails = "";
                    angular.forEach(result.data.errors, function (value, key) {
                        errorDetails += key + " => " + value;
                    });
                    errorDialog = MyDialogs.error("データ登録・更新失敗", errorDetails);
                    break;

                case 404:
                    errorDialog = MyDialogs.error("データ登録・更新失敗", result.data.message);
                    break;

                default:
                    errorDialog = MyDialogs.serverError("データ登録・更新失敗", result);
                    break;
            }

            return errorDialog;
        };

        // 変更を反映する
        var applyChanges = function (customHandler) {

            uiBlocker.start();

            vm.currentGroup.syncServer()
            .then(function(value){
                console.log("syncServer success");
                console.log(value);

                uiBlocker.stop();

                if (customHandler && customHandler.success) {
                    customHandler.success();
                }
            })
            ["catch"](function(responseHeaders) {
                console.log("syncServer error");
                console.log(responseHeaders);

                uiBlocker.stop();

                var dialog = showErrorDialog(responseHeaders);

                dialog.result["finally"](function(config){
                    if (customHandler && customHandler.error) {
                        customHandler.error();
                    }
                });
            });
        };

        vm.categories = [
            { id: 'bento', name: 'お弁当' },
            { id: 'side',  name: 'サイドメニュー' }
        ];

        vm.week = createWeek(initialData.beginDay);

        vm.currentGroup = new DayGroup($filter, $q, initialData.currentDay, initialData.currentMenu);

        vm.datePickerSettings = {
            currentDay: toUTCDate(vm.currentGroup.day),
            opened: false,
            isUnavailableDay: function(day, mode) {
                return ( mode === 'day' && ( day.getDay() === 0 || day.getDay() === 6 ) );
            },
            datePickerOptions: {
                showWeeks: false
            }
        };

        $scope.$watch(function(){
            return vm.datePickerSettings.currentDay;
        }, function(newVal, oldVal){
            if (oldVal.valueOf() === newVal.valueOf()) {
                return;
            }

            var localDate = moment({ year: newVal.getUTCFullYear(), month: newVal.getUTCMonth(), day: newVal.getUTCDate()});
            $location.path("/admin/daily-menus/" + localDate.format("YYYY-MM-DD"));
        });

        //---- イベントハンドラ (カレンダー)
        // カレンダーを表示する
        vm.showCalendar = function($event) {
            $event.preventDefault();
            $event.stopPropagation();

            vm.datePickerSettings.opened = true;
        };

        // 日付(タブ)の選択
        vm.chooseDay = function (menuDate) {
            this.datePickerSettings.currentDay = toUTCDate(menuDate);
        };

        //---- イベントハンドラ (ステータス)
        vm.changeMenuStatus = function (status) {
            vm.currentGroup.menu.status = status;
            applyChanges();
        };

        //---- イベントハンドラ (商品)
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
                        return vm.currentGroup.getItems(category);
                    },
                    currentItem: function () {
                        return vm.currentGroup.getItem(category, index);
                    }
                }
            });

            modalInstance.result.then(function (selectedItem) {
                vm.currentGroup.setItem(category, index, selectedItem);
                applyChanges();
            }, function () {
                console.log('Modal dismissed at: ' + new Date());
            });
        };

        vm.resetItem = function(category, index) {
            vm.currentGroup.resetItem(category, index);
            applyChanges();
        };

        //---- イベントハンドラ (注文編集) ここにあるべきではないような…
        vm.editOrder = function (dailyMenu) {
            var modalInstance = $modal.open({
                templateUrl: Assets.versioned("/views/admin/daily-order/edit"),
                controller: "DailyOrderEditController",
                controllerAs: "vm",
                size: 'lg',
                resolve: app.my.resolvers.DailyOrderEditController(vm.currentGroup.day)
            });

            modalInstance.result
            ["catch"](function(result) {
                if (result && result.caller === "resolver") { // dismissの場合に表示しないよう、発生源を確認する
                    MyDialogs.serverError("ダイアログ表示失敗", result);
                }
            });
        };

        //---- 状態 日付を選択しているか?
        vm.isDaySelected = function (day) {
            if (vm.currentGroup.menu === undefined) {
                return false;
            }
            return (vm.currentGroup.day.valueOf() === day.valueOf());
        };

        vm.classForMenuItem = function (category, index) {
            return {
                'empty': !vm.currentGroup.itemIsSelected(category, index),
                'selected': vm.currentGroup.itemIsSelected(category, index)
            };
        };

    }

    app.my.resolvers.DailyMenuEditController = {
        initialData: function($route, $q, DailyMenu) {

            var menuDate = moment().startOf('week').add(1, "days").add(1, "weeks").format("YYYY-MM-DD");
            if ($route.current.params.menuDate !== undefined) {
                menuDate = $route.current.params.menuDate;
            }

            var currentDate = moment.tz(menuDate, moment.defaultZone.name); //日付のみの文字をパースするときはTimezoneを指定しないと、OSのデフォルトに影響される

            var deferred = $q.defer();
            var initialData = {};

            initialData.beginDay = moment(currentDate).startOf('week').add(1, "days");
            initialData.endDay = moment(initialData.beginDate).add(4, "days");
            initialData.currentDay = currentDate;

            DailyMenu.getByMenuDate({
                menuDate: app.my.helpers.formatTimestamp(initialData.currentDay)
            }).$promise
            .then(function(value){
                initialData.currentMenu = value;

                // データがない場合はデータを生成する
                if (!initialData.currentMenu || initialData.currentMenu.id === null) {
                    initialData.currentMenu = DailyMenu.createEmptyData(initialData.currentDay);
                }

                deferred.resolve(initialData);
            })
            ["catch"](function(responseHeaders) {
                deferred.reject(responseHeaders);
            });

            return deferred.promise;
        }
    };
})();
