
angular.module('MyControllers')
.controller('AdminChecklistController',
    ['$scope', '$location', '$routeParams', '$filter', 'User', 'DailyMenu', 'DailyOrder',
    function ($scope, $location, $routeParams, $filter, User, DailyMenu, DailyOrder) {

    // チェックリストに使うデータの作成
    var createChecklist = function () {

        var checklist = [];

        // その日注文しているユーザごとに姓名と、注文状況を調査する
        angular.forEach($scope.dailyOrders, function (order) {

            var checklistItem = [];
            checklistItem.userName = order.localUser.lastName + " " + order.localUser.firstName;
            var orderStatuses = [];

            angular.forEach(order.detailItems, function (item) {
                var orderStatus = [];
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
        checklist = $filter('orderBy')(checklist, ["orderStatuses_bits", "userName"]);

        return checklist;
    };

    $scope.menuDate = moment.utc($routeParams.menuDate);

    var params = {menuDate: $scope.menuDate.format('YYYY-MM-DD')};

    DailyMenu.getByMenuDate(params,
        function (response) {
            // 店名、品名の順に並び替え
            response.detailItems = $filter('orderBy')(response.detailItems, ['menuItem.shopName', 'menuItem.name']);
            $scope.dailyMenu = response;

            $scope.dailyOrders = DailyOrder.getByOrderDate({orderDate: $scope.menuDate.format('YYYY-MM-DD')},
                function (response) {
                    $scope.checklist = createChecklist();
                },
                function (response) {
                    if (response.status === 404) {
                        //FIXME: データが存在しなかった場合は戻らないと
                        return;
                    } else {
                        alert("注文のデータが取得できませんでした。");
                    }
                }
            );
        },
        function (response) {
            if (response.status === 404) {
                //FIXME: データが存在しなかった場合は戻らないと
                return;
            } else {
                alert("メニューのデータが取得できませんでした。");
            }
        }
    );

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
}]);

