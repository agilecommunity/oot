
angular.module('MyControllers')
.controller('OrderController',
    ['$scope', '$location', '$filter', '$modal', 'User', 'DailyMenu', 'DailyOrder', 'initialData',
    function ($scope, $location, $filter, $modal, User, DailyMenu, DailyOrder, initialData) {

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

        var orderErrorHandler = function(response){
            console.log(response);
            bootbox.alert("注文に失敗しました。status:" + response.status, function () {});
        };

        if (target !== null) {
            if (new_state === true) {
                target.detailItems.push({menuItem: dailyMenuItem.menuItem, numOrders: 1});
            } else {
                target.detailItems = target.detailItems.filter(function (item, index) {
                    return (item.menuItem.id !== dailyMenuItem.menuItem.id);
                });
            }

            // あった場合は更新する
            if (target.detailItems.length > 0) {
                target.$update({}, function(){}, orderErrorHandler);
            } else {
                target.$delete({}, function(){
                    $scope.dailyOrders = $scope.dailyOrders.filter(function(order, index){
                        return (order.id !== target.id);
                    }, orderErrorHandler);
                });
            }
        } else {
            // ない場合は新しく作る
            var new_order = new DailyOrder();
            new_order.orderDate = dailyMenu.menuDate;
            new_order.localUser = User.currentUser();
            new_order.detailItems = [
                {menuItem: dailyMenuItem.menuItem, numOrders: 1}
            ];

            new_order.$create(function(){
                $scope.dailyOrders.push(new_order);
            }, orderErrorHandler);
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