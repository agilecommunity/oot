
angular.module('MyControllers')
.controller('OrderController',
    ['$scope', '$location', '$filter', '$modal', 'dialogs', 'User', 'DailyMenu', 'DailyOrder', 'initialData',
    function ($scope, $location, $filter, $modal, dialogs, User, DailyMenu, DailyOrder, initialData) {

    // 初期データの取得
    $scope.dailyMenus = initialData.dailyMenus;
    $scope.dailyOrders = initialData.dailyOrders;
    $scope.dailyOrdersThisWeek = initialData.dailyOrdersThisWeek;

    // 今週の日を一覧にする
    var startDayThisWeek = moment().startOf('week').add(1, "days");
    $scope.daysThisWeek = [
        startDayThisWeek,
        moment(startDayThisWeek).add(1, "days"),
        moment(startDayThisWeek).add(2, "days"),
        moment(startDayThisWeek).add(3, "days"),
        moment(startDayThisWeek).add(4, "days")
    ];

    // メニューに注文状況を反映する
    var applyOrdered = function () {
        if ($scope.dailyMenus === undefined || $scope.dailyOrders === undefined) {
            return;
        }
        console.log("#applyOrdered daily_menus:" + $scope.dailyMenus.length);
        console.log("#applyOrdered daily_orders:" + $scope.dailyOrders.length);

        // メニューの注文状況をリセットする
        angular.forEach($scope.dailyMenus, function (menu) {
            angular.forEach(menu.detailItems, function (item) {
                item.ordered = false;
            });
        });

        // 注文を見ながらメニューの注文状況を変更する
        angular.forEach($scope.dailyOrders, function (order) {
            var menu = $filter('getByMenuDate')($scope.dailyMenus, order.orderDate);
            console.log("#applyOrdered date:" + app.my.helpers.formatTimestamp(order.orderDate) + " menu:" + menu);
            if (menu !== null) {
                angular.forEach(order.detailItems, function (orderDetailItem) {
                    angular.forEach(menu.detailItems, function (menuDetailItem) {
                        if (orderDetailItem.menuItem.id === menuDetailItem.menuItem.id) {
                            menuDetailItem.ordered = true;
                        }
                    });
                });
            }
        });
    };

    $scope.order = function (dailyMenu, dailyMenuItem) { // イベントハンドラ

        // メニューの注文状況を切り替える
        var new_state = dailyMenuItem.ordered !== true;
        dailyMenuItem.ordered = new_state;

        // 注文オブジェクトがあるかどうかを調べる
        var target = $filter('getByOrderDate')($scope.dailyOrders, dailyMenu.menuDate);

        var operation = target === null ? "create" : "update";

        var backup = {
            dailyOrder: angular.copy(target)
        };

        var errorHandler = function(result) {
            console.log(result);

            var errorDialog = null;
            switch (result.status) {
                case 422:
                    var errorDetails = "";
                    angular.forEach(result.data.errors, function(value, key){
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

            errorDialog.result["finally"](function(config){
                // 対象のオブジェクトをいったん削除
                console.log($scope.dailyOrders);
                $scope.dailyOrders = $scope.dailyOrders.filter(function(order, index){
                    // 更新対象のorderのorderDateが文字列になっているので moment で強制的に変換
                    return (dailyMenu.menuDate.valueOf() !== moment(order.orderDate).valueOf());
                });
                // 更新・削除だったばあいは元に戻す
                if (operation === "update") {
                    $scope.dailyOrders.push(backup.dailyOrder);
                }
                // 状態を反映する
                applyOrdered();
            });
        };

        if (operation === "update") {
            if (new_state === true) {
                target.detailItems.push({menuItem: dailyMenuItem.menuItem, numOrders: 1});
            } else {
                target.detailItems = target.detailItems.filter(function (item, index) {
                    return (item.menuItem.id !== dailyMenuItem.menuItem.id);
                });
            }

            // あった場合は更新する
            if (target.detailItems.length > 0) {
                target.$update({}, function(){}, errorHandler);
            } else {
                target.$delete({}, function(){
                    $scope.dailyOrders = $scope.dailyOrders.filter(function(order, index){
                        return (order.id !== target.id);
                    });
                }, errorHandler);
            }
        } else {
            // ない場合は新しく作る
            target = new DailyOrder();
            target.orderDate = dailyMenu.menuDate;
            target.localUser = User.currentUser();
            target.detailItems = [
                {menuItem: dailyMenuItem.menuItem, numOrders: 1}
            ];

            target.$create(function(){
                $scope.dailyOrders.push(target);
            }, errorHandler);
        }
    };

    $scope.editNumOrders = function(dailyMenu, dailyMenuItem) {

        var modalInstance = $modal.open({
            templateUrl: "/views/select-num-orders",
            controller: "SelectNumOrdersController"
        });

        modalInstance.result.then(function (numOrders) {
            var order = $filter('getByOrderDate')($scope.dailyOrders, dailyMenu.menuDate);

            if (order === null) {
                return;
            }

            var orderItem = order.findItem(dailyMenuItem.menuItem);

            if (orderItem === null) {
                return;
            }

            orderItem.numOrders = numOrders;

            order.$update({});

        }, function () {
        });
    };

    $scope.numOrders = function(dailyMenu, dailyMenuItem) {
        var order = $filter('getByOrderDate')($scope.dailyOrders, dailyMenu.menuDate);

        if (order === null) {
            return "";
        }

        var orderItem = order.findItem(dailyMenuItem.menuItem);

        if (orderItem === null) {
            return "";
        }

        return orderItem.numOrders;
    };

    $scope.totalReducedOfTheDay = function (targetDate) {
        var order = $filter('getByOrderDate')($scope.dailyOrders, targetDate);
        if (order !== null) {
            return order.totalReducedOnOrder();
        }
        return 0;
    };

    $scope.getDetailItems = function(dailyMenu, category) {
        var items = $filter('filter')(dailyMenu.detailItems, {menuItem: {category: category}});
        return items;
    };

    $scope.hasDetailItems = function(dailyMenu, category) {
        return $scope.getDetailItems(dailyMenu, category).length > 0;
    };

    $scope.getOrderThisWeek = function(targetDate) {
        return $filter('getByOrderDate')($scope.dailyOrdersThisWeek, targetDate);
    };

    // 画像を表示するHTMLを出力
    $scope.renderImage = function(dailyMenuItem) {
        var imgFile = "no-image.png";
        if ($filter("isEmptyOrUndefined")(dailyMenuItem.menuItem.itemImagePath) !== true ) {
            imgFile = dailyMenuItem.menuItem.itemImagePath;
        }
        return "<img src=\"/uc-assets/images/menu-items/" + imgFile + "\" alt=\"...\">";
    };

    $scope.renderTotalReducedOnOrderThisWeek = function(targetDate) {
        var order = $scope.getOrderThisWeek(targetDate);
        if (order === null) {
            return "";
        }
        return order.totalReducedOnOrder() + "円";
    };

    // メニュー、または注文の内容が変わった場合は、メニューの注文状況を反映しなおす
    $scope.$watchCollection("dailyMenus", applyOrdered);
    $scope.$watchCollection("dailyOrders", applyOrdered);
}]);

app.my.resolvers.OrderController = {
    initialData: function($route, $q, DailyMenu, DailyOrder) {

        var deferred = $q.defer();
        var initialData = {};

        DailyMenu.queryByStatus({
            status: "open"
        }).$promise
        .then(function(value){
            initialData.dailyMenus = value;

            return DailyOrder.queryMine({
                status: "open"
            }).$promise;
        })
        .then(function(value) {
            initialData.dailyOrders = value;

            var today = moment();
            var dateBegin = moment(today).startOf('week').add(1, "days"); // startOfは日曜が取れるので月曜にシフト
            var dateEnd = moment(dateBegin).add(4, "days");

            return DailyOrder.queryMine({
                "from": app.my.helpers.formatTimestamp(dateBegin),
                "to": app.my.helpers.formatTimestamp(dateEnd)
            }).$promise;
        })
        .then(function(value) {
            initialData.dailyOrdersThisWeek = value;
            deferred.resolve(initialData);
        })
        ["catch"](function(responseHeaders) {
            deferred.reject({status: responseHeaders.status, reason: responseHeaders.data});
        });

        return deferred.promise;
    }
};