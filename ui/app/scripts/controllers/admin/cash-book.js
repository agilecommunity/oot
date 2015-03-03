angular.module('MyControllers')
.controller('AdminCashBookController',
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

        // 1週間＝5日分のデータを作成する
        $scope.cashBooks = [];
        for (var index=0; index <5; index++) {
            var targetDate = moment($scope.startDate).add(index, "days");

            // 登録されているデータを検索
            var targetMenu = $filter('getByMenuDate')(initialData.dailyMenus, targetDate);

            // データがない場合はダミーのデータを作成
            if (targetMenu === null) {
                targetMenu = DailyMenu.createEmptyData(targetDate);
            }
            $scope.cashBooks.push({menu: targetMenu, targetDate: targetDate});
        }

        angular.forEach($scope.cashBooks, function(cashBook){
            var detailItems = [];
            angular.forEach(cashBook.menu.detailItems, function(detailItem){
                var numOrders = getAmount(cashBook.menu.menuDate, detailItem.menuItem);

                // 0個の場合でも登録する
                detailItem.numOrders = numOrders;
                detailItems.push(detailItem);
            });
            cashBook.detailItems = detailItems;
            cashBook.isEmpty = function() {
                return this.detailItems.length === 0;
            };
        });
    };

    setUp();
}]);

app.my.resolvers.AdminCashBookController = {
    initialData: function($route, $q, DailyMenu, DailyOrderAggregate) {
        var startDate = moment.tz($route.current.params.targetDate, moment.defaultZone.name); //日付のみの文字をパースするときはTimezoneを指定しないと、OSのデフォルトに影響される
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
