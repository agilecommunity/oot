(function(){

    angular.module('MyControllers')
        .controller('AdminChecklistDailyController', AdminChecklistDailyController);

    AdminChecklistDailyController.$inject = ["initialData"];

    function AdminChecklistDailyController(initialData) {
        var vm = this;

        vm.menuDate = initialData.menuDate;
        vm.dailyMenu = initialData.dailyMenu;
        vm.dailyOrders = initialData.dailyOrders;
    }

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
})();

