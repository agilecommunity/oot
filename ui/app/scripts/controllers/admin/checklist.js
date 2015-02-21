
angular.module('MyControllers')
.controller('AdminChecklistController',
    ['$scope', '$location', '$routeParams', '$filter', 'User', 'DailyMenu', 'DailyOrder',
    function ($scope, $location, $routeParams, $filter, User, DailyMenu, DailyOrder) {

    // 注文状況の作成
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

        // 自分の注文した商品を探しやすくするために、注文した人を商品でグルーピングする
        // ※ 複数商品を注文した人は、最初の商品(並び順的に)を使ってグルーピングする
        var prevGroup = -1;
        var orderGroup = 0;
        angular.forEach(checklist, function(checkItem){
            if (prevGroup !== checkItem.orderStatusesBits.indexOf("0")) {
                orderGroup += 1;
                prevGroup = checkItem.orderStatusesBits.indexOf("0");
            }
            checkItem.orderGroup = orderGroup;
        });

        return checklist;
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

    $scope.menuDate = moment.tz($routeParams.menuDate, moment.defaultZone.name); // 日付のみの文字をパースする場合、TimeZoneを指定しないとOSのタイムゾーンが影響するらしい

    var params = {menuDate: app.my.helpers.formatTimestamp($scope.menuDate)};

    DailyMenu.getByMenuDate(params,
        function (response) {
            // カテゴリ、店名、品名の順に並び替え(カテゴリはbento, sideの順に表示したい)
            response.detailItems = $filter('orderBy')(response.detailItems, ['menuItem.category', 'menuItem.shopName', 'menuItem.name']);
            $scope.dailyMenu = response;

            $scope.dailyOrders = DailyOrder.queryByOrderDate({orderDate: app.my.helpers.formatTimestamp($scope.menuDate)},
                function (response) {
                    // メニューと注文状況を使ってチェックリストに必要なデータを作成する
                    $scope.checklist = createChecklist();
                    $scope.itemList = createItemList();
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

