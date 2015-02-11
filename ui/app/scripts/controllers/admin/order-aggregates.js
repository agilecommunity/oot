
angular.module('MyControllers')
    .controller('AdminOrderAggregatesController',
    ['$scope', 'DailyOrderAggregate', 'initialData',
    function ($scope, DailyOrderAggregate, initialData) {

    $scope.orderDate = initialData.orderDate;
    $scope.orderAggregates = initialData.orderAggregates;
    $scope.dailyMenu = initialData.dailyMenu;

    $scope.getNumOrders = function(menuItem) {
        if ($scope.orderAggregates === undefined || $scope.orderAggregates === null) {
            return 0;
        }
        var index = DailyOrderAggregate.findByMenuItem($scope.orderAggregates, menuItem);
        if (index === -1) {
            return 0;
        }
        return $scope.orderAggregates[index].numOrders;
    };
}]);

app.my.resolvers.AdminOrderAggregatesController = {
    initialData: function($route, $q, DailyMenu, DailyOrderAggregate) {
        var orderDate = moment.tz($route.current.params.orderDate, moment.defaultZone.name); //日付のみの文字をパースするときはTimezoneを指定しないと、OSのデフォルトに影響される

        var deferred = $q.defer();
        var initialData = {};
        initialData.orderDate = orderDate;

        DailyOrderAggregate.queryByOrderDate({
            orderDate: app.my.helpers.formatTimestamp(orderDate)
        }).$promise
        .then(function(value) {
            initialData.orderAggregates = value;
            return DailyMenu.getByMenuDate({
                menuDate: app.my.helpers.formatTimestamp(orderDate)
            }).$promise;
        })
        .then(function(value) {
            initialData.dailyMenu = value;
            deferred.resolve(initialData);
        })
        .catch(function(responseHeaders) {
            deferred.reject({status: responseHeaders.status, reason: responseHeaders.data});
        });

        return deferred.promise;
    }
};
