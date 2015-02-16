
angular.module('MyControllers')
.controller('UserIndexController',
        ['$scope', '$location', '$routeParams', '$filter', '$modal', 'usSpinnerService', 'User', 'initialData',
function ($scope, $location, $routeParams, $filter, $modal, usSpinnerService, User, initialData) {

    $scope.users = initialData.users;

}]);

app.my.resolvers.UserIndexController = {
    initialData: function($route, $q, User) {

        var deferred = $q.defer();
        var initialData = {};

        User.query({
        }).$promise
        .then(function(value) {
            initialData.users = value;
            deferred.resolve(initialData);
        })
        ["catch"](function(responseHeaders) {
            deferred.reject({status: responseHeaders.status, reason: responseHeaders.data});
        });

        return deferred.promise;
    }
};
