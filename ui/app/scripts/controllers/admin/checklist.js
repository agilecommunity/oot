
angular.module('MyControllers')
.controller('AdminChecklistController',
    ['$scope', '$location', '$routeParams', '$filter', 'User', 'DailyMenu', 'DailyOrder', 'initialData',
    function ($scope, $location, $routeParams, $filter, User, DailyMenu, DailyOrder, initialData) {

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

    var setUp = function(){
        $scope.menuDate = initialData.menuDate;
        $scope.dailyMenu = initialData.dailyMenu;
        $scope.dailyOrders = initialData.dailyOrders;

        $scope.dailyMenu.detailItems = $filter('orderBy')($scope.dailyMenu.detailItems, ['menuItem.category', 'menuItem.shopName', 'menuItem.name']);

        $scope.checklist = createChecklist();
        $scope.itemList = createItemList();
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
}]);

app.my.resolvers.AdminChecklistController = {
    initialData: function($route, $q, DailyMenu, DailyOrder) {
        var menuDate = moment.tz($route.current.params.menuDate, moment.defaultZone.name); //日付のみの文字をパースするときはTimezoneを指定しないと、OSのデフォルトに影響される

        var deferred = $q.defer();
        var initialData = {};

        initialData.menuDate = menuDate;

        DailyMenu.getByMenuDate({
            menuDate: app.my.helpers.formatTimestamp(menuDate)
        }).$promise
        .then(function(value) {
            initialData.dailyMenu = value;
            return DailyOrder.queryByOrderDate({
                orderDate: app.my.helpers.formatTimestamp(menuDate)
            }).$promise;
        })
        .then(function(value) {
            initialData.dailyOrders = value;
            deferred.resolve(initialData);
        })
        ["catch"](function(responseHeaders) {
        deferred.reject({status: responseHeaders.status, reason: responseHeaders.data});
        });

        return deferred.promise;
    }
};
