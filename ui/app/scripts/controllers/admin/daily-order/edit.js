
angular.module('MyControllers')
    .controller('DailyOrderEditController',
    ['$scope', '$location', '$routeParams', '$filter', '$modal', 'User', 'MenuItem', 'DailyMenu', 'DailyOrder', 'dailyMenu',
    function ($scope, $location, $routeParams, $filter, $modal, User, MenuItem, DailyMenu, DailyOrder, dailyMenu) {

    // チェックリストに使うデータの作成
    var createChecklist = function () {

        var checklist = [];

        // その日注文しているユーザごとに姓名と、注文状況を調査する
        angular.forEach($scope.dailyOrders, function (order) {

            var checklistItem = [];
            checklistItem.order = order;
            checklistItem.userName = order.localUser.lastName + " " + order.localUser.firstName;
            var orderStatuses = [];

            angular.forEach(order.detailItems, function (item) {
                var orderStatus = {};
                orderStatus.menuId = item.menuItem.id;
                orderStatus.numOrders = item.numOrders;
                orderStatuses[item.menuItem.id] = orderStatus;
            });
            checklistItem.orderStatuses = orderStatuses;
            checklist.push(checklistItem);
        });

        // 同じ商品を買った人は続けて表示されるよう、全商品の注文状況を文字列にまとめソートキーとする
        angular.forEach(checklist, function(checkItem){
            var orderStatusesBits = "";
            angular.forEach($scope.dailyMenu.detailItems, function(menuItem){
                if (checkItem.orderStatuses[menuItem.menuItem.id] !== undefined) {
                    orderStatusesBits += "0";
                } else {
                    orderStatusesBits += "1";
                }
            });
            checkItem.orderStatusesBits = orderStatusesBits;
        });
        // 全商品の注文状況と、ユーザ名でソートする
        checklist = $filter('orderBy')(checklist, ["orderStatusesBits", "userName"]);

        return checklist;
    };

    $scope.checklist = [];
    $scope.dailyMenu = dailyMenu;
    $scope.dailyOrders = DailyOrder.queryByOrderDate({orderDate: app.my.helpers.formatTimestamp($scope.dailyMenu.menuDate)},
        function (response) {
            $scope.checklist = createChecklist();
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

    $scope.editItem = function(order, menuItem, orderStatuses) {

        var modalInstance = $modal.open({
            templateUrl: "/views/select-num-orders",
            controller: "SelectNumOrdersController"
        });

        modalInstance.result.then(function (numOrders) {
            var orderItem = order.findItem(menuItem);

            if (orderItem === null) {
                order.addItem(menuItem, numOrders);
                orderStatuses[menuItem.id] = {menuId: menuItem.id, numOrders: numOrders};
            } else {
                orderItem.numOrders = numOrders;
            }

            orderStatuses[menuItem.id].numOrders = numOrders;

            order.$save({});

        }, function () {
        });
    };

    $scope.deleteItem = function(order, menuItem, orderStatuses, event) {
        event.stopPropagation();
        event.preventDefault();

        order.removeItem(menuItem);
        orderStatuses[menuItem.id] = undefined;

        if (order.detailItems.length !== 0) {
            order.$update({});
        } else {
            order.$delete({});
        }
    };

    $scope.addUser = function() {

        var modalInstance = $modal.open({
            templateUrl: "/views/admin/select-user",
            controller: "SelectUserController"
        });

        modalInstance.result.then(function (user) {
            var newOrder = new DailyOrder();
            newOrder.orderDate = $scope.dailyMenu.menuDate;
            newOrder.localUser = user;
            newOrder.detailItems = [];

            var checklistItem = [];
            checklistItem.order = newOrder;
            checklistItem.userName = newOrder.localUser.lastName + " " + newOrder.localUser.firstName;
            checklistItem.orderStatuses = [];

            $scope.dailyOrders.push(checklistItem.order);
            $scope.checklist.push(checklistItem);
        });
    };

}]);