
angular.module('MyControllers')
.controller('OrderController',
    ['$scope', '$location', '$filter', '$modal', 'User', 'DailyMenu', 'DailyOrder',
    function ($scope, $location, $filter, $modal, User, DailyMenu, DailyOrder, isEmptyOrUndefinedFilter) {

    $scope.dailyMenus = DailyMenu.queryByStatus({status: "open"},
        function (response) { // 成功時
            $scope.dailyOrders = DailyOrder.queryMine({},
                function (response) { // 成功時
                },
                function (response) {   // 失敗時
                    alert("注文データが取得できませんでした。サインイン画面に戻ります。");
                    $location.path("/");
                }
            );
        },
        function (response) {   // 失敗時
            alert("メニューのデータが取得できませんでした。サインイン画面に戻ります。");
            $location.path("/");
        }
    );

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

    // 画像を表示するHTMLを出力
    $scope.renderImage = function(dailyMenuItem) {
        var imgFile = "no-image.png";
        if ($filter("isEmptyOrUndefined")(dailyMenuItem.menuItem.itemImagePath) !== true ) {
            imgFile = dailyMenuItem.menuItem.itemImagePath;
        }
        return "<img src=\"/uc-assets/images/menu-items/" + imgFile + "\" alt=\"...\">";
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

    $scope.totalPriceOfTheDay = function (targetDate) {
        var order = $filter('getByOrderDate')($scope.dailyOrders, targetDate);
        if (order !== null) {
            return order.totalPrice();
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

    // メニュー、または注文の内容が変わった場合は、メニューの注文状況を反映しなおす
    $scope.$watchCollection("dailyMenus", applyOrdered);
    $scope.$watchCollection("dailyOrders", applyOrdered);
}]);

