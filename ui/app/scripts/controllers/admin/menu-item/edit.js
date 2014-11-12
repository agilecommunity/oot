angular.module('MyControllers')
    .controller('MenuItemEditController',
    ['$scope', '$location', '$routeParams', '$filter', '$modal', 'User', 'MenuItem',
    function ($scope, $location, $routeParams, $filter, $modal, User, MenuItem) {

    var menu_item_saved = angular.copy($scope.menu_item);

    $scope.errors = [];

    $scope.save = function() {
        var handler = {};
        handler.success = function(saved) {
            $scope.errors = [];
            $scope.menu_item.id = saved.id;
            $scope.$close();
        };
        handler.error = function(error) {
            console.log(error);
            if (error.status == 400) {
                $scope.errors = error.data;
            } else {
                alert("データが保存できませんでした。");
                $scope.$close();
            }
        };

        $scope.menu_item.$save({}, handler.success, handler.error);
    };

    $scope.cancel = function() {
        angular.copy(menu_item_saved, $scope.menu_item);
        $scope.$dismiss();
    };

    $scope.set_category = function(value) {
        $scope.menu_item.category = value;
    };

    $scope.set_status = function(value) {
        $scope.menu_item.status = value;
    };

    $scope.has_error = function(name) {
        if ($scope.errors.length === 0) {
            return false;
        }

        return ($scope.errors[name] !== undefined && $scope.errors[name] !== null);
    };

}]);