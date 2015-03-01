angular.module('MyControllers')
    .controller('MenuItemEditController',
    ['$scope', '$location', '$routeParams', '$filter', '$modal', 'dialogs', 'User', 'MenuItem', 'menuItem',
    function ($scope, $location, $routeParams, $filter, $modal, dialogs, User, MenuItem, menuItem) {

    $scope.menuItem = menuItem;
    var menuItemSaved = angular.copy($scope.menuItem);

    $scope.errors = [];

    $scope.save = function() {
        var handler = {};

        if ($scope.menuItem.id === undefined) {
            handler.success = function(saved) {
                $scope.$close(saved[0]);
            };

            handler.error = function(error) {
                console.log(error);
                if (error.status == 422) {
                    $scope.errors = error.data.errors[0];
                } else {
                    var dialog = dialogs.error("データ登録・更新失敗", error.data.message);

                    dialog.result["finally"](function(){
                        $scope.$close();
                    });
                }
            };

            MenuItem.create({}, [$scope.menuItem], handler.success, handler.error);
        } else {
            handler.success = function(saved) {
                $scope.$close(saved);
            };

            handler.error = function(error) {
                console.log(error);
                if (error.status === 422) {
                    $scope.errors = error.data.errors;
                } else {
                    var dialog = dialogs.error("データ登録・更新失敗", error.data.message);

                    dialog.result["finally"](function(){
                        $scope.$close();
                    });
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