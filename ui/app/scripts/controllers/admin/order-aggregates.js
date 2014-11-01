angular.module('MyControllers')
    .controller('AdminOrderAggregatesController',
    ['$scope', '$location', '$routeParams', '$filter', 'User', 'DailyOrderAggregate',
        function ($scope, $location, $routeParams, $filter, User, DailyOrderAggregate) {

    $scope.order_date = moment.utc($routeParams.order_date);

    var params = {order_date: $scope.order_date.format('YYYY-MM-DD')};

    $scope.order_aggregates = DailyOrderAggregate.getByOrderDate(params,
        function (response) {
        },
        function (response) {
            alert("データが取得できませんでした。");
        }
    );
}]);

