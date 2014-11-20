angular.module('MyControllers')
    .controller('AdminOrderAggregatesController',
    ['$scope', '$location', '$routeParams', '$filter', 'User', 'DailyOrderAggregate', 'orderDate', 'orderAggregates',
        function ($scope, $location, $routeParams, $filter, User, DailyOrderAggregate, orderDate, orderAggregates) {

    $scope.orderDate = orderDate;
    $scope.orderAggregates = orderAggregates;

}]);

