
angular.module('MyControllers')
    .controller('AdminOrderAggregatesController',
    ['$scope', 'initialData',
    function ($scope, initialData) {

    $scope.orderDate = initialData.orderDate;
    $scope.orderAggregates = initialData.orderAggregates;

}]);

app.my.resolvers.AdminOrderAggregatesController = {
    initialData: function($route, $q, DailyOrderAggregate) {
        var orderDate = moment.tz($route.current.params.orderDate, moment.defaultZone.name); //日付のみの文字をパースするときはTimezoneを指定しないと、OSのデフォルトに影響される

        var deferred = $q.defer();

        var success = function(value, responseHeaders) {
            deferred.resolve({orderDate: orderDate, orderAggregates: value});
        };

        var error = function(responseHeaders) {
            deferred.reject({status: responseHeaders.status, reason: responseHeaders.data});
        };

        DailyOrderAggregate.queryByOrderDate({
            orderDate: app.my.helpers.formatTimestamp(orderDate)
        }).$promise.then(success, error);

        return deferred.promise;
    }
};
