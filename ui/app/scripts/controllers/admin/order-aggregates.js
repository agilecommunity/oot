
angular.module('MyControllers')
    .controller('AdminOrderAggregatesController',
    ['$scope', 'orderDate', 'orderAggregates',
    function ($scope, orderDate, orderAggregates) {

    $scope.orderDate = orderDate;
    $scope.orderAggregates = orderAggregates;

}]);

app.my.resolvers.AdminOrderAggregatesController = {
    orderDate: function($route) {
        return moment.utc($route.current.params.orderDate);
    },
    orderAggregates: function($route, $q, DailyOrderAggregate) {
        var deferred = $q.defer();

        var success = function(value, responseHeaders) {
            deferred.resolve(value);
        };
        var error = function(responseHeaders) {
            deferred.reject({status: responseHeaders.status, reason: responseHeaders.data});
        };

        var orderDate = moment.utc($route.current.params.orderDate);
        DailyOrderAggregate.getByOrderDate({
            orderDate: orderDate.format('YYYY-MM-DD')
        }, success, error);

        return deferred.promise;
    }
};
