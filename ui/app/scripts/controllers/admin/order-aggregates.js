angular.module('MyControllers')
    .controller('AdminOrderAggregatesController',
    ['$scope', '$location', '$routeParams', '$filter', 'User', 'DailyOrderAggregate',
        function ($scope, $location, $routeParams, $filter, User, DailyOrderAggregate) {

    $scope.orderDate = moment.utc($routeParams.orderDate);

    var params = {orderDate: $scope.orderDate.format('YYYY-MM-DD')};

    $scope.orderAggregates = DailyOrderAggregate.getByOrderDate(params,
        function (response) {
        },
        function (response) {
            alert("データが取得できませんでした。");
        }
    );
}]);

