
angular.module('MyDirectives')
.directive('ootChecklistDetail', function ($compile, $filter) {

    return {
        restrict: "A",
        templateUrl: "/views/_partial/_checklist-detail",
        scope: {
            dailyMenu: "=dailymenu",
            dailyOrders: "=dailyorders"
        },
        controller: ['$scope', function ($scope) {
            // 注文状況の作成
            var createChecklist = function () {

                var checklist = {detailItems: [], totalReducedOnOrder: 0};

                // その日注文しているユーザごとに姓名と、注文状況を調査する
                angular.forEach($scope.dailyOrders, function (order) {

                    var checklistItem = {};
                    checklistItem.userName = order.localUser.lastName + " " + order.localUser.firstName;
                    var orderStatuses = [];

                    angular.forEach(order.detailItems, function (item) {
                        var orderStatus = {};
                        orderStatus.menuId = item.menuItem.id;
                        orderStatus.numOrders = item.numOrders;
                        orderStatuses[item.menuItem.id] = orderStatus;
                    });

                    checklistItem.orderStatuses = orderStatuses;
                    checklist.detailItems.push(checklistItem);

                    checklist.totalReducedOnOrder += order.totalReducedOnOrder();
                });

                // 同じ商品を買った人は続けて表示されるよう、全商品の注文状況を文字列にまとめソートキーとする
                angular.forEach(checklist.detailItems, function(checkItem){
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
                checklist.detailItems = $filter('orderBy')(checklist.detailItems, ["orderStatusesBits", "userName"]);

                // 自分の注文した商品を探しやすくするために、注文した人を商品でグルーピングする
                // ※ 複数商品を注文した人は、最初の商品(並び順的に)を使ってグルーピングする
                var prevGroup = -1;
                var orderGroup = 0;
                angular.forEach(checklist.detailItems, function(checkItem){
                    if (prevGroup !== checkItem.orderStatusesBits.indexOf("0")) {
                        orderGroup += 1;
                        prevGroup = checkItem.orderStatusesBits.indexOf("0");
                    }
                    checkItem.orderGroup = orderGroup;
                });

                return checklist;
            };

            var setUp = function() {
                $scope.dailyMenu.detailItems = $filter('orderBy')($scope.dailyMenu.detailItems, ['menuItem.category', 'menuItem.shopName', 'menuItem.name']);

                $scope.checklist = createChecklist();
                $scope.itemlist = createItemList();
            };

            // 注文のあった商品のリストの作成
            var createItemList = function() {
                var orderdItemIds = [];
                angular.forEach($scope.dailyOrders, function (order) {
                    angular.forEach(order.detailItems, function (item) {
                        orderdItemIds.push(item.menuItem.id);
                    });
                });
                orderdItemIds = _.uniq(orderdItemIds);

                var itemList = [];
                angular.forEach($scope.dailyMenu.detailItems, function(detailItem){
                    if (_.contains(orderdItemIds, detailItem.menuItem.id)) {
                        itemList.push(detailItem.menuItem);
                    }
                });

                return itemList;
            };

            $scope.menuOrOrderIsEmpty = function() {
                if ($scope.dailyMenu === undefined) {
                    return true;
                }

                if ($scope.dailyMenu.detailItems === null || $scope.dailyMenu.detailItems.length === 0)
                {
                    return true;
                }

                if ($scope.dailyOrders.length === 0) {
                    return true;
                }

                return false;
            };

            $scope.renderNumOrders = function(numOrders) {
                if ($filter('isEmptyOrUndefined')(numOrders) === true) {
                    return "";
                }

                if (numOrders === 1) {
                    return "○";
                }

                return numOrders;
            };

            setUp();
        }]
    };
});
