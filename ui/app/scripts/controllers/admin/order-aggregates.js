
angular.module('MyControllers')
    .controller('AdminOrderAggregatesController',
    ['$scope', 'initialData',
    function ($scope, initialData) {

    $scope.orderDate = initialData.orderDate;
    $scope.orderAggregates = initialData.orderAggregates;

}]);

app.my.resolvers.AdminOrderAggregatesController = {
    initialData: function($route, $q, DailyOrderAggregate) {
        var orderDate = moment.utc($route.current.params.orderDate);

        var deferred = $q.defer();

        var success = function(value, responseHeaders) {
            deferred.resolve({orderDate: orderDate, orderAggregates: value});
        };

        var error = function(responseHeaders) {
            deferred.reject({status: responseHeaders.status, reason: responseHeaders.data});
        };

        DailyOrderAggregate.queryByOrderDate({
            orderDate: orderDate.format('YYYY-MM-DD')
        }).$promise.then(success, error);

        return deferred.promise;
    }
};
