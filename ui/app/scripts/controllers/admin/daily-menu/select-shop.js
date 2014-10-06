angular.module('MyControllers')
    .controller('DailyMenuSelectShopController',
    ['$scope', '$location', '$routeParams', '$filter', '$modal', 'User', 'Shop',
    function ($scope, $location, $routeParams, $filter, $modal, User, Shop) {

    $scope.shops = Shop.query({});

    $scope.selectThis = function(item) {
        $scope.$close(item);
    };
}]);