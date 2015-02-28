
angular.module('MyControllers')
.controller('AdminChecklistWeeklyController',
['$scope', '$location', '$routeParams', '$filter', 'User', 'DailyMenu', 'DailyOrder', 'initialData',
function ($scope, $location, $routeParams, $filter, User, DailyMenu, DailyOrder, initialData) {

    var setUp = function(){
        $scope.startDate = initialData.startDate;
        $scope.endDate = initialData.endDate;
        $scope.groupedList = initialData.groupedList;
    };

    setUp();
}]);

app.my.resolvers.AdminChecklistWeeklyController = {
    initialData: function($route, $q, $filter, DailyMenu, DailyOrder) {
        var startDate = moment.tz($route.current.params.startDate, moment.defaultZone.name); //日付のみの文字をパースするときはTimezoneを指定しないと、OSのデフォルトに影響される
        var endDate = moment(startDate).add(4, "days");

        var deferred = $q.defer();
        var initialData = {};

        initialData.startDate = startDate;
        initialData.endDate = endDate;
        initialData.groupedList = [];

        DailyMenu.query({
            from: app.my.helpers.formatTimestamp(startDate),
            to: app.my.helpers.formatTimestamp(endDate)
        }).$promise
        .then(function(value) {
            for (var index=0; index <5; index++) {
                var targetDate = moment(initialData.startDate).add(index, "days");

                // 登録されているデータを検索
                var targetMenu = $filter('getByMenuDate')(value, targetDate);

                // データがない場合はダミーのデータを作成
                if (targetMenu === null) {
                    targetMenu = DailyMenu.createEmptyData(targetDate);
                }
                initialData.groupedList.push({targetDate: targetMenu.menuDate, dailyMenu: targetMenu, dailyOrders: []});
            }

            return DailyOrder.query({
                from: app.my.helpers.formatTimestamp(startDate),
                to: app.my.helpers.formatTimestamp(endDate)
            }).$promise;
        })
        .then(function(value) {
            angular.forEach(initialData.groupedList, function(item){
                item.dailyOrders = DailyOrder.filterByOrderDate(value, item.targetDate);
            });

            deferred.resolve(initialData);
        })
        ["catch"](function(responseHeaders) {
            deferred.reject({status: responseHeaders.status, reason: responseHeaders.data});
        });

        return deferred.promise;
    }
};
