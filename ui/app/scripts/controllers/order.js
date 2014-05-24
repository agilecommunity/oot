
angular.module('MyControllers')
.controller('OrderController',
    ['$scope', '$location', '$filter', 'User', 'DailyMenu', 'DailyOrder',
    function ($scope, $location, $filter, User, DailyMenu, DailyOrder) {

    $scope.daily_menus = DailyMenu.queryByStatus({status: "open"},
        function (response) { // 成功時
            $scope.daily_orders = DailyOrder.query({},
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

    $scope.order = function (daily_menu, daily_menu_item) { // イベントハンドラ

        // メニューの注文状況を切り替える
        var new_state = daily_menu_item.ordered !== true;
        daily_menu_item.ordered = new_state;

        // 注文オブジェクトがあるかどうかを調べる
        var order = $filter('getByOrderDate')($scope.daily_orders, daily_menu.menu_date);

        var reload_orders = function (request) {
            // 念のためサーバから最新の注文を取得する
            $scope.daily_orders = DailyOrder.query();
        };

        if (order !== null) {
            if (new_state === true) {
                order.detail_items.push({menu_item: daily_menu_item.menu_item});
            } else {
                order.detail_items = order.detail_items.filter(function (item, index) {
                    return (item.menu_item.id !== daily_menu_item.menu_item.id);
                });
            }

            // あった場合は更新する
            if (order.detail_items.length > 0) {
                order.$update({}, reload_orders);
            } else {
                order.$delete({}, reload_orders);
            }
        } else {
            // ない場合は新しく作る
            var new_order = new DailyOrder();
            new_order.order_date = daily_menu.menu_date.unix() * 1000;
            new_order.local_user = User.current_user();
            new_order.detail_items = [
                {menu_item: daily_menu_item.menu_item}
            ];

            DailyOrder.create({}, [new_order], reload_orders);
        }
    };

    $scope.totalPriceOfTheDay = function (target_date) {
        var order = $filter('getByOrderDate')($scope.daily_orders, target_date);
        if (order !== null) {
            return order.total_price();
        }
        return 0;
    };

    // メニューに注文状況を反映する
    var applyOrdered = function () {
        // メニューの注文状況をリセットする
        angular.forEach($scope.daily_menus, function (menu) {
            angular.forEach(menu.detail_items, function (item) {
                item.ordered = false;
            });
        });
        // 注文を見ながらメニューの注文状況を変更する
        angular.forEach($scope.daily_orders, function (order) {
            var menu = $filter('getByMenuDate')($scope.daily_menus, order.order_date);
            if (menu !== null) {
                angular.forEach(order.detail_items, function (o_d_item) {
                    angular.forEach(menu.detail_items, function (m_d_item) {
                        if (o_d_item.menu_item.id === m_d_item.menu_item.id) {
                            m_d_item.ordered = true;
                        }
                    });
                });
            }
        });
    };

    // メニュー、または注文の内容が変わった場合は、メニューの注文状況を反映しなおす
    $scope.$watchCollection("daily_menus", applyOrdered);
    $scope.$watchCollection("daily_orders", applyOrdered);
}]);

