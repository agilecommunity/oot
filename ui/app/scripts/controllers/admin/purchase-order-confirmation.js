angular.module('MyControllers')
.controller('AdminPurchaseOrderConfirmationController',
    ['$scope', '$filter', 'DailyMenu', 'initialData',
    function ($scope, $filter, DailyMenu, initialData) {

    var setUp = function() {
        $scope.startDate = initialData.startDate;
        $scope.endDate = initialData.endDate;

        // 1週間＝5日分のデータを作成する
        $scope.dailyMenus = [];
        for (var index=0; index <5; index++) {
            var targetDate = moment($scope.startDate).add(index, "days");

            // 登録されているデータを検索
            var targetMenu = $filter('getByMenuDate')(initialData.dailyMenus, targetDate);

            // データがない場合はダミーのデータを作成
            if (targetMenu === null) {
                targetMenu = DailyMenu.createEmptyData(targetDate);
            }

            $scope.dailyMenus.push(targetMenu);
        }
    };

    setUp();

}]);

app.my.resolvers.AdminPurchaseOrderConfirmationController = {
    initialData: function($route, $q, DailyMenu, DailyOrder) {
        var startDate = moment.tz($route.current.params.orderDate, moment.defaultZone.name); //日付のみの文字をパースするときはTimezoneを指定しないと、OSのデフォルトに影響される
        var endDate = moment(startDate).add(4, "days");

        var deferred = $q.defer();
        var initialData = {};
        initialData.startDate = startDate;
        initialData.endDate = endDate;

        DailyMenu.query({
            from: app.my.helpers.formatTimestamp(startDate),
            to: app.my.helpers.formatTimestamp(endDate)
        }).$promise
        .then(function(value) {
            initialData.dailyMenus = value;
            deferred.resolve(initialData);
        })
        ["catch"](function(responseHeaders) {
            deferred.reject({status: responseHeaders.status, reason: responseHeaders.data});
        });

        return deferred.promise;
    }
};
