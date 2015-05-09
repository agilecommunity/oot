(function(){

    angular.module('MyControllers')
        .controller('DailyOrderEditController', DailyOrderEditController);

    DailyOrderEditController.$inject = ['$scope', '$filter', '$modal', 'MyDialogs', 'DailyOrder', 'Assets', 'dailyMenu'];

    function DailyOrderEditController($scope, $filter, $modal, MyDialogs, DailyOrder, Assets, dailyMenu) {

        var vm = this;

        // チェックリストに使うデータの作成
        var createChecklist = function () {

            var checklist = [];

            // その日注文しているユーザごとに姓名と、注文状況を調査する
            angular.forEach(vm.dailyOrders, function (order) {

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
                angular.forEach(vm.dailyMenu.detailItems, function(menuItem){
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

        vm.checklist = [];
        vm.dailyMenu = dailyMenu;
        vm.dailyOrders = DailyOrder.queryByOrderDate({orderDate: app.my.helpers.formatTimestamp(vm.dailyMenu.menuDate)},
            function (response) {
                vm.checklist = createChecklist();
            },
            function (response) {
                if (response.status === 404) {
                    return [];
                } else {
                    var errorDialog = MyDialogs.error("データ取得失敗", "注文データが取得できませんでした");

                    errorDialog.result["finally"](function(config){
                        $scope.$dismiss("注文データが取得できませんでした。");
                    });
                }
            }
        );

        vm.editItem = function(order, menuItem, orderStatuses) {

            var modalInstance = $modal.open({
                templateUrl: Assets.versioned("/views/select-num-orders"),
                controller: "SelectNumOrdersController",
                controllerAs: "vm"
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

        vm.deleteItem = function(order, menuItem, orderStatuses, event) {
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

        vm.addUser = function() {

            var modalInstance = $modal.open({
                templateUrl: Assets.versioned("/views/admin/select-user"),
                controller: "SelectUserController",
                controllerAs: "vm",
                resolve: app.my.resolvers.SelectUserController
            });

            modalInstance.result.then(function (user) {
                var newOrder = new DailyOrder();
                newOrder.orderDate = vm.dailyMenu.menuDate;
                newOrder.localUser = user;
                newOrder.detailItems = [];

                var checklistItem = [];
                checklistItem.order = newOrder;
                checklistItem.userName = newOrder.localUser.lastName + " " + newOrder.localUser.firstName;
                checklistItem.orderStatuses = [];

                vm.dailyOrders.push(checklistItem.order);
                vm.checklist.push(checklistItem);
            })
            ["catch"](function(reason) {
                if (reason && reason.source === "resolve") { // dismissの場合に表示しないよう、発生源を確認する
                    MyDialogs.resolveError("ダイアログ表示失敗", reason);
                }
            });
        };
    }

})();
