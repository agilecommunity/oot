
angular.module('MyControllers')
.controller('AdminChecklistController',
    ['$scope', '$location', '$routeParams', '$filter', 'User', 'DailyMenu', 'DailyOrder',
    function ($scope, $location, $routeParams, $filter, User, DailyMenu, DailyOrder) {

    // チェックリストに使うデータの作成
    var createChecklist = function () {

        var checklist = [];

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

    $scope.menuDate = moment.tz($routeParams.menuDate, moment.defaultZone.name); // 日付のみの文字をパースする場合、TimeZoneを指定しないとOSのタイムゾーンが影響するらしい

    var params = {menuDate: app.my.helpers.formatTimestamp($scope.menuDate)};

    DailyMenu.getByMenuDate(params,
        function (response) {
            // カテゴリ、店名、品名の順に並び替え(カテゴリはbento, sideの順に表示したい)
            response.detailItems = $filter('orderBy')(response.detailItems, ['menuItem.category', 'menuItem.shopName', 'menuItem.name']);
            $scope.dailyMenu = response;

            $scope.dailyOrders = DailyOrder.queryByOrderDate({orderDate: app.my.helpers.formatTimestamp($scope.menuDate)},
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

    $scope.totalReducedOnOrder = function() {
        var total = 0;
        angular.forEach($scope.dailyOrders, function (order) {
            total += order.totalReducedOnOrder();
        });
        return total;
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

