(function(){

    angular.module('MyControllers')
        .controller('ViewOrderController', ViewOrderController);

    ViewOrderController.$inject = ['$filter', 'initialData'];

    function ViewOrderController($filter, initialData) {
        var vm = this;

        vm.currentDate = initialData.currentDate;

        vm.beginDate = initialData.beginDate;
        vm.endDate = initialData.endDate;

        vm.allCurrentDay = initialData.allCurrentDay;

        vm.daysCurrentWeek = [];
        for (var i=0; i<5; i++) {
            var day = moment(vm.beginDate).add(i, "days");
            var dailyOrder = $filter('getByOrderDate')(initialData.mineCurrentWeek.dailyOrders, day);

            vm.daysCurrentWeek.push({ day: day, dailyOrder: dailyOrder});

        }

        vm.firstDayLastWeek = moment(vm.beginDate).subtract(1, "weeks");
        vm.firstDayNextWeek = moment(vm.beginDate).add(1, "weeks");
        vm.firstDayThisWeek = moment().startOf('week').add(1, "days");

        vm.isCurrentDay = function(day) {
            return day.valueOf() === vm.currentDate.valueOf();
        };

        vm.hasOrder = function(order) {
            return (order !== undefined && order !== null && order.detailItems.length > 0);
        };
    }

    app.my.resolvers.ViewOrderController = {
        initialData: function($route, $q, $filter, DailyMenu, DailyOrder) {

            var menuDate = moment().format("YYYY-MM-DD");
            if ($route.current.params.menuDate !== undefined) {
                menuDate = $route.current.params.menuDate;
            }

            var currentDate = moment.tz(menuDate, moment.defaultZone.name); //日付のみの文字をパースするときはTimezoneを指定しないと、OSのデフォルトに影響される

            var deferred = $q.defer();
            var initialData = {};

            initialData.currentDate = currentDate;

            initialData.beginDate = moment(currentDate).startOf('week').add(1, "days");
            initialData.endDate = moment(initialData.beginDate).add(4, "days");

            initialData.allCurrentDay = {};
            initialData.mineCurrentWeek = {};

            DailyMenu.getByMenuDate({
                menuDate: app.my.helpers.formatTimestamp(currentDate)
            }).$promise
            .then(function(value) {
                initialData.allCurrentDay.dailyMenu = value;
                return DailyOrder.queryByOrderDate({
                    orderDate: app.my.helpers.formatTimestamp(currentDate)
                }).$promise;
            })
            .then(function(value) {
                initialData.allCurrentDay.dailyOrders = value;

                return DailyOrder.queryMine({
                    "from": app.my.helpers.formatTimestamp(initialData.beginDate),
                    "to": app.my.helpers.formatTimestamp(initialData.endDate)
                }).$promise;
            })
            .then(function(value) {
                initialData.mineCurrentWeek.dailyOrders = value;
                deferred.resolve(initialData);
            })
            ["catch"](function(responseHeaders) {
                deferred.reject(responseHeaders);
            });

            return deferred.promise;
        }
    };

})();