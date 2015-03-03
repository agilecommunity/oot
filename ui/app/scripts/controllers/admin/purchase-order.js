angular.module('MyControllers')
.controller('AdminPurchaseOrderController',
    ['$scope', '$filter', 'DailyMenu', 'DailyOrderAggregate', 'initialData',
    function ($scope, $filter, DailyMenu, DailyOrderAggregate, initialData) {

    var getAmount = function(targetDate, menuItem) {
        var aggregate = DailyOrderAggregate.find($scope.orderAggregates, targetDate, menuItem);
        if (aggregate === null) {
            return 0;
        }
        return aggregate.numOrders;
    };

    var setUp = function() {
        $scope.startDate = initialData.startDate;
        $scope.endDate = initialData.endDate;
        $scope.orderAggregates = initialData.orderAggregates;

        var additionalItems = [
            {name: "送料", numOrders: 1, totalFixedOnPurchaseIncTax: 540}
        ];

        // 1週間＝5日分のデータを作成する
        $scope.purchaseOrders = [];
        for (var index=0; index <5; index++) {
            var targetDate = moment($scope.startDate).add(index, "days");

            // 登録されているデータを検索
            var targetMenu = $filter('getByMenuDate')(initialData.dailyMenus, targetDate);

            // データがない場合はダミーのデータを作成
            if (targetMenu === null) {
                targetMenu = DailyMenu.createEmptyData(targetDate);
            }
            $scope.purchaseOrders.push({menu: targetMenu, orderDate: targetMenu.menuDate, additionalItems: additionalItems});
        }

        angular.forEach($scope.purchaseOrders, function(order){
            var detailItems = [];
            var daySubTotalNumOrders = 0;
            var daySubTotalFixedOnPurchaseIncTax = 0;
            angular.forEach(order.menu.detailItems, function(detailItem){
                var numOrders = getAmount(order.menu.menuDate, detailItem.menuItem);

                if (numOrders > 0) {
                    detailItem.numOrders = numOrders;
                    detailItem.totalFixedOnPurchaseIncTax = detailItem.menuItem.fixedOnPurchaseIncTax * detailItem.numOrders;
                    detailItems.push(detailItem);

                    daySubTotalNumOrders += detailItem.numOrders;
                    daySubTotalFixedOnPurchaseIncTax += detailItem.totalFixedOnPurchaseIncTax;
                }
            });
            order.detailItems = detailItems;
            order.daySubTotalNumOrders = daySubTotalNumOrders;
            order.daySubTotalFixedOnPurchaseIncTax = daySubTotalFixedOnPurchaseIncTax;

            var dayTotalNumOrders = order.daySubTotalNumOrders;
            var dayTotalFixedOnPurchaseIncTax = order.daySubTotalFixedOnPurchaseIncTax;

            angular.forEach(order.additionalItems, function(item){
                dayTotalNumOrders += item.numOrders;
                dayTotalFixedOnPurchaseIncTax += item.totalFixedOnPurchaseIncTax;
            });
            order.dayTotalNumOrders = dayTotalNumOrders;
            order.dayTotalFixedOnPurchaseIncTax = dayTotalFixedOnPurchaseIncTax;

            order.isEmpty = function() {
                return this.detailItems.length === 0;
            };
        });
    };

    setUp();
}]);

app.my.resolvers.AdminPurchaseOrderController = {
    initialData: function($route, $q, DailyMenu, DailyOrderAggregate) {
        var startDate = moment.tz($route.current.params.orderDate, moment.defaultZone.name); //日付のみの文字をパースするときはTimezoneを指定しないと、OSのデフォルトに影響される
        var endDate = moment(startDate).add(4, "days");

        var deferred = $q.defer();
        var initialData = {};
        initialData.startDate = startDate;
        initialData.endDate = endDate;

        DailyMenu.query({
            from: app.my.helpers.formatTimestamp(startDate),
            to: app.my.helpers.formatTimestamp(endDate)
        }).$promise
        .then(function(value) {
            initialData.dailyMenus = value;
            return DailyOrderAggregate.query({
                from: app.my.helpers.formatTimestamp(startDate),
                to: app.my.helpers.formatTimestamp(endDate)
            }).$promise;
        })
        .then(function(value) {
            initialData.orderAggregates = value;
            deferred.resolve(initialData);
        })
        ["catch"](function(responseHeaders) {
            deferred.reject({status: responseHeaders.status, reason: responseHeaders.data});
        });

        return deferred.promise;
    }
};
