
angular.module('MyControllers')
    .controller('DailyOrderEditController',
    ['$scope', '$location', '$routeParams', '$filter', '$modal', 'User', 'MenuItem', 'DailyMenu', 'DailyOrder', 'daily_menu',
    function ($scope, $location, $routeParams, $filter, $modal, User, MenuItem, DailyMenu, DailyOrder, daily_menu) {

    // チェックリストに使うデータの作成
    var create_checklist = function () {

        var checklist = [];

        // その日注文しているユーザごとに姓名と、注文状況を調査する
        angular.forEach($scope.daily_orders, function (order) {

            var checklist_item = [];
            checklist_item.order = order;
            checklist_item.user_name = order.local_user.last_name + " " + order.local_user.first_name;
            var order_statuses = [];

            angular.forEach(order.detail_items, function (item) {
                var order_status = {};
                order_status.menu_id = item.menu_item.id;
                order_status.num_orders = item.num_orders;
                order_statuses[item.menu_item.id] = order_status;
            });
            checklist_item.order_statuses = order_statuses;
            checklist.push(checklist_item);
        });

        // 同じ商品を買った人は続けて表示されるよう、全商品の注文状況を文字列にまとめソートキーとする
        angular.forEach(checklist, function(check_item){
            var order_statuses_bits = "";
            angular.forEach($scope.daily_menu.detail_items, function(menu_item){
                if (check_item.order_statuses[menu_item.menu_item.id] !== undefined) {
                    order_statuses_bits += "0";
                } else {
                    order_statuses_bits += "1";
                }
            });
            check_item.order_statuses_bits = order_statuses_bits;
        });
        // 全商品の注文状況と、ユーザ名でソートする
        checklist = $filter('orderBy')(checklist, ["order_statuses_bits", "user_name"]);

        return checklist;
    };

    $scope.checklist = [];
    $scope.daily_menu = daily_menu;
    $scope.daily_orders = DailyOrder.getByOrderDate({order_date: $scope.daily_menu.menu_date.format('YYYY-MM-DD')},
        function (response) {
            $scope.checklist = create_checklist();
        },
        function (response) {
            if (response.status === 404) {
                return [];
            } else {
                alert("注文データが取得できませんでした。");
                $dismiss("注文データが取得できませんでした。");
            }
        }
    );

    $scope.edit_item = function(order, menu_item, order_statuses) {

        var modalInstance = $modal.open({
            templateUrl: "/views/select-num-orders",
            controller: "SelectNumOrdersController"
        });

        modalInstance.result.then(function (num_orders) {
            var order_item = order.find_item(menu_item);

            if (order_item === null) {
                order.add_item(menu_item, num_orders);
                order_statuses[menu_item.id] = {menu_id: menu_item.id, num_orders: num_orders};
            } else {
                order_item.num_orders = num_orders;
            }

            order_statuses[menu_item.id].num_orders = num_orders;

            if (order.id === undefined) {
                order.$create({});
            } else {
                order.$update({});
            }

        }, function () {
        });
    };

    $scope.delete_item = function(order, menu_item, order_statuses, event) {
        event.stopPropagation();
        event.preventDefault();

        order.remove_item(menu_item);
        order_statuses[menu_item.id] = undefined;

        if (order.detail_items.length !== 0) {
            order.$update({});
        } else {
            order.$delete({});
        }
    };

    $scope.add_user = function() {

        var modalInstance = $modal.open({
            templateUrl: "/views/admin/select-user",
            controller: "SelectUserController"
        });

        modalInstance.result.then(function (user) {
            var new_order = new DailyOrder();
            new_order.order_date = $scope.daily_menu.menu_date;
            new_order.local_user = user;
            new_order.detail_items = [];

            var checklist_item = [];
            checklist_item.order = new_order;
            checklist_item.user_name = new_order.local_user.last_name + " " + new_order.local_user.first_name;
            checklist_item.order_statuses = [];

            $scope.daily_orders.push(checklist_item.order);
            $scope.checklist.push(checklist_item);
        });
    };

}]);