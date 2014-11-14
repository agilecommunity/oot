angular.module('MyControllers')
    .controller('SelectNumOrdersController',
    ['$scope',
    function ($scope) {

    $scope.select_this = function(value) {
        $scope.$close(value);
    };

}]);