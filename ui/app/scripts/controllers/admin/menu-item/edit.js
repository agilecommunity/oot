angular.module('MyControllers')
    .controller('MenuItemEditController',
    ['$scope', '$location', '$routeParams', '$filter', '$modal', 'User', 'MenuItem',
    function ($scope, $location, $routeParams, $filter, $modal, User, MenuItem) {

    var menuItemSaved = angular.copy($scope.menuItem);

    $scope.errors = [];

    $scope.save = function() {
        var handler = {};

        if ($scope.menuItem.id === undefined) {
            handler.success = function(saved) {
                $scope.errors = [];
                $scope.menuItem.id = saved[0].id;
                $scope.$close();
            };

            handler.error = function(error) {
                console.log(error);
                if (error.status == 400) {
                    $scope.errors = error.data[0];
                } else {
                    alert("データが保存できませんでした。");
                    $scope.$close();
                }
            };

            MenuItem.create([$scope.menuItem], handler.success, handler.error);
        } else {
            handler.success = function(saved) {
                $scope.errors = [];
                $scope.menuItem.id = saved.id;
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
            $scope.menuItem.$update({}, handler.success, handler.error);
        }
    };

    $scope.cancel = function() {
        angular.copy(menuItemSaved, $scope.menuItem);
        $scope.$dismiss();
    };

    $scope.setCategory = function(value) {
        $scope.menuItem.category = value;
    };

    $scope.setStatus = function(value) {
        $scope.menuItem.status = value;
    };

    $scope.hasError = function(name) {
        if ($scope.errors.length === 0) {
            return false;
        }

        return ($scope.errors[name] !== undefined && $scope.errors[name] !== null);
    };

}]);