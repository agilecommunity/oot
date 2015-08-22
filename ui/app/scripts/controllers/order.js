(function(){

    var DayGroup = function($filter) {
        this.$filter = $filter;

        this.day = null;
        this.menu = null;
        this.order = null;
        this.orderStat = null;
        this.orderStatuses = [];
    };

    DayGroup.prototype.setData = function(day, menu, order, orderStat) {
        this.day = day;
        this.menu = menu;
        this.order = order;
        this.orderStat = orderStat;

        this.updateOrderStatus();
    };

    DayGroup.prototype.setOrderStat = function(value) {
        this.orderStat = value;
        this.updateOrderStatus();
    };

    DayGroup.prototype.totalReducedOnOrder = function (targetDate) {
        if (this.order !== null) {
            return this.order.totalReducedOnOrder();
        }
        return 0;
    };

    DayGroup.prototype.numOrders = function(menuItem) {
        return this.orderStatuses[menuItem.id];
    };

    DayGroup.prototype.hasOrder = function(menuItem) {
        return (this.orderStatuses[menuItem.id] > 0);
    };

    DayGroup.prototype.getDetailItems = function(category) {
        var items = this.$filter('filter')(this.menu.detailItems, {menuItem: {category: category}});
        return items;
    };

    DayGroup.prototype.hasDetailItems = function(category) {
        return vm.getDetailItems(this.menu, category).length > 0;
    };

    DayGroup.prototype.updateOrderStatus = function() {
        this.orderStatuses = [];

        var that = this;
        angular.forEach(that.menu.detailItems, function(detailItem){
            var numOrders = 0;

            if (that.order !== null) {
                var orderItem = that.order.findItem(detailItem.menuItem);

                if (orderItem !== null) {
                    numOrders = orderItem.numOrders;
                }
            }

            that.orderStatuses[detailItem.menuItem.id] = numOrders;
        });
    };

    angular.module('MyControllers')
        .controller('OrderController', OrderController);

    OrderController.$inject = ['$scope', '$filter', '$modal', 'MyDialogs', 'User', 'DailyOrder', 'DailyOrderStat', 'Assets', 'initialData'];

    function OrderController($scope, $filter, $modal, MyDialogs, User, DailyOrder, DailyOrderStat, Assets, initialData) {

        var vm = this;

        vm.categories = [
            { id: 'bento', name: 'お弁当' },
            { id: 'side',  name: 'サイドメニュー' }
        ];

        vm.gatheringSetting = initialData.gatheringSetting;
        vm.dailyOrderStats = initialData.dailyOrderStats;
        vm.orderRanking = initialData.orderRanking;

        vm.dayGroups = [];
        angular.forEach(initialData.dailyMenus, function(dailyMenu){
            var day = dailyMenu.menuDate;

            var dailyOrder = $filter('getByOrderDate')(initialData.dailyOrders, day);

            var dailyOrderStat = $filter('getByOrderDate')(vm.dailyOrderStats, day);
            if (dailyOrderStat === null) {
                dailyOrderStat = DailyOrderStat.createEmptyData();
            }

            var dayGroup = new DayGroup($filter);
            dayGroup.setData(day, dailyMenu, dailyOrder, dailyOrderStat);

            vm.dayGroups.push(dayGroup);
        });

        var showErrorDialog = function(result) {

            var errorDialog = null;
            switch (result.status) {
                case 422:
                    var errorDetails = "";
                    angular.forEach(result.data.errors, function(value, key){
                        errorDetails += key + " => " + value;
                    });
                    errorDialog = MyDialogs.error("データ登録・更新失敗", errorDetails);
                    break;

                case 404:
                    errorDialog = MyDialogs.error("データ登録・更新失敗", result.data.message);
                    break;

                case 403:
                    errorDialog = MyDialogs.error("データ登録・更新失敗", "メニューが準備中または締めきられました");
                    break;

                default:
                    errorDialog = MyDialogs.serverError("データ登録・更新失敗", result);
                    break;
            }

            return errorDialog;
        };

        var updateOrderStat = function(dayGroup) {
            DailyOrderStat.query({
                orderDate: app.my.helpers.formatTimestamp(dayGroup.day)
            }, function(response){
                dayGroup.setOrderStat(response[0]);
            });
        };

        var createOrder = function(dayGroup, menuItem) {

            var order = new DailyOrder();
            order.orderDate = dayGroup.day;
            order.localUser = User.currentUser();
            order.detailItems = []; // $resourceにはコンストラクタがない(っぽい)ので初期化するタイミングがない
            order.addItem(menuItem, 1);

            var errorHandler = function(result) {
                var errorDialog = showErrorDialog(result);

                errorDialog.result["finally"](function(config){
                    dayGroup.order = null;
                });
            };

            order.$create(function(){
                dayGroup.order = order;
                updateOrderStat(dayGroup);
            }, errorHandler);
        };

        var updateOrder = function(dayGroup, menuItem) {

            var order = dayGroup.order;

            var backup = {
                order: angular.copy(order)
            };

            var menuItemOperation = dayGroup.hasOrder(menuItem) ? "delete" : "create";

            var errorHandler = function(result) {
                var errorDialog = showErrorDialog(result);

                errorDialog.result["finally"](function(config){
                    dayGroup.order = backup.order;
                });
            };

            if (menuItemOperation === "create") {
                order.addItem(menuItem, 1);
            } else if (menuItemOperation === "delete") {
                order.removeItem(menuItem);
            }

            // あった場合は更新する
            if (! order.isEmpty()) {
                order.$update({}, function(){
                    updateOrderStat(dayGroup);
                }, errorHandler);

            } else {
                order.$delete({}, function(){
                    dayGroup.order = null;
                    updateOrderStat(dayGroup);
                }, errorHandler);
            }
        };

        vm.order = function (dayGroup, menuItem) { // イベントハンドラ

            var order = dayGroup.order;

            var orderOperation = order === null ? "create" : "update";

            if (orderOperation === "create") {
                createOrder(dayGroup, menuItem);
            } else if (orderOperation === "update") {
                updateOrder(dayGroup, menuItem);
            }
        };

        vm.editNumOrders = function(dayGroup, menuItem) {

            var order = dayGroup.order;

            var backup = {
                order: angular.copy(order)
            };

            var modalInstance = $modal.open({
                templateUrl: Assets.versioned("/views/select-num-orders"),
                controller: "SelectNumOrdersController",
                controllerAs: "vm"
            });

            modalInstance.result.then(function (numOrders) {
                if (order === null) {
                    return;
                }

                var orderItem = order.updateItem(menuItem, numOrders);

                order.$update({}, function(){
                    updateOrderStat(dayGroup);
                }, function(result){
                    var errorDialog = showErrorDialog(result);

                    errorDialog.result["finally"](function(config){
                        dayGroup.order = backup.order;
                    });
                });
            }, function () {
            });
        };

        vm.inOrderRanking = function(menuItem) {
            return vm.orderRanking.getResult(menuItem) !== null;
        };

        vm.getRank = function(menuItem) {
            var result = vm.orderRanking.getResult(menuItem);
            if (result === null) {
                return "";
            }
            return result.rank;
        };
    }

    app.my.resolvers.OrderController = {
        initialData: function($route, $q, DailyMenu, DailyOrder, GatheringSetting, DailyOrderStat, OrderRanking) {

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
            .then(function(value){
                initialData.dailyOrders = value;

                return DailyOrderStat.query({
                    status: "open"
                }).$promise;
            })
            .then(function(value) {
                initialData.dailyOrderStats = value;

                return GatheringSetting.get({
                }).$promise;
            })
            .then(function(value) {
                initialData.gatheringSetting = value;

                return OrderRanking.getLastMonth({
                }).$promise;
            })
            .then(function(value) {
                initialData.orderRanking = value;
                deferred.resolve(initialData);
            })
            ["catch"](function(responseHeaders) {
                deferred.reject(responseHeaders);
            });

            return deferred.promise;
        }
    };
})();
