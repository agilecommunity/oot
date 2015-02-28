
angular.module('MyControllers')
.controller('AdminChecklistDailyController',
    ['$scope', '$location', '$routeParams', '$filter', 'User', 'DailyMenu', 'DailyOrder', 'initialData',
    function ($scope, $location, $routeParams, $filter, User, DailyMenu, DailyOrder, initialData) {

    var setUp = function(){
        $scope.menuDate = initialData.menuDate;
        $scope.dailyMenu = initialData.dailyMenu;
        $scope.dailyOrders = initialData.dailyOrders;
    };

    setUp();
}]);

app.my.resolvers.AdminChecklistDailyController = {
    initialData: function($route, $q, DailyMenu, DailyOrder) {
        var menuDate = moment.tz($route.current.params.menuDate, moment.defaultZone.name); //日付のみの文字をパースするときはTimezoneを指定しないと、OSのデフォルトに影響される

        var deferred = $q.defer();
        var initialData = {};

        initialData.menuDate = menuDate;

        DailyMenu.getByMenuDate({
            menuDate: app.my.helpers.formatTimestamp(menuDate)
        }).$promise
        .then(function(value) {
            initialData.dailyMenu = value;
            return DailyOrder.queryByOrderDate({
                orderDate: app.my.helpers.formatTimestamp(menuDate)
            }).$promise;
        })
        .then(function(value) {
            initialData.dailyOrders = value;
            deferred.resolve(initialData);
        })
        ["catch"](function(responseHeaders) {
        deferred.reject({status: responseHeaders.status, reason: responseHeaders.data});
        });

        return deferred.promise;
    }
};
