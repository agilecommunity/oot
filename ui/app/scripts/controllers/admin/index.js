(function(){

    angular.module('MyControllers')
        .controller('AdminIndexController', AdminIndexController);

    AdminIndexController.$inject = ['$location', 'initialData', 'DailyMenu', 'DailyOrderStat'];

    function AdminIndexController($location, initialData, DailyMenu, DailyOrderStat) {

        var vm = this;

        var createWeek = function(label, startDay, dailyMenus, orderStats) {

            var days = [];
            for(var index=0; index<5; index++) {
                var currentDay = moment(startDay).add(index, "days");
                var currentMenu = DailyMenu.find(dailyMenus, currentDay);
                var currentStatus = currentMenu === null ? "未作成" : currentMenu.statusText();
                var currentOrderStat = DailyOrderStat.find(orderStats, currentDay);

                if (currentOrderStat === null) {
                    currentOrderStat = DailyOrderStat.createEmptyData(currentDay);
                }

                days.push({
                    day: currentDay,
                    menu: currentMenu,
                    status: currentStatus,
                    orderStat: currentOrderStat
                });
            }

            return {
                label: label,
                startDay: startDay,
                endDay: _.last(days).day,
                days: days
            };
        };

        vm.weeks = [
            createWeek("来週", initialData.startDayOfNextWeek, initialData.dailyMenus, initialData.dailyOrderStats),
            createWeek("今週", initialData.startDayOfThisWeek, initialData.dailyMenus, initialData.dailyOrderStats),
            createWeek("先週", initialData.startDayOfLastWeek, initialData.dailyMenus, initialData.dailyOrderStats)
        ];

        vm.showPurchaseOrderConfirmation = function (targetDay) {
            $location.path("/admin/purchase-order/" + targetDay.format('YYYY-MM-DD') + "/confirmation");
        };

        vm.showPurchaseOrder = function (targetDay) {
            $location.path("/admin/purchase-order/" + targetDay.format('YYYY-MM-DD'));
        };

        vm.showCashBook = function (targetDay) {
            $location.path("/admin/cash-book/" + targetDay.format('YYYY-MM-DD'));
        };

        vm.showChecklistDaily = function (targetDay) {
            $location.path("/admin/checklist/" + targetDay.format('YYYY-MM-DD'));
        };

        vm.showChecklistWeekly = function (targetDay) {
            $location.path("/admin/checklist/weekly/" + targetDay.format('YYYY-MM-DD'));
        };

    }

    app.my.resolvers.AdminIndexController = {
        initialData: function($q, DailyMenu, DailyOrderStat) {

            var startDayOfThisWeek = moment().startOf('week').add(1, "days");
            var startDayOfNextWeek = moment(startDayOfThisWeek).add(1, "weeks");
            var startDayOfLastWeek = moment(startDayOfThisWeek).add(-1, "weeks");

            var deferred = $q.defer();
            var initialData = {};

            initialData.startDayOfThisWeek = startDayOfThisWeek;
            initialData.startDayOfNextWeek = startDayOfNextWeek;
            initialData.startDayOfLastWeek = startDayOfLastWeek;

            var startDate = startDayOfLastWeek;
            var endDate = moment(startDayOfNextWeek).add(4, "days");

            DailyMenu.query({
                from: app.my.helpers.formatTimestamp(startDate),
                to: app.my.helpers.formatTimestamp(endDate)
            }).$promise
            .then(function(value) {
                initialData.dailyMenus = value;
                return DailyOrderStat.query({
                    from: app.my.helpers.formatTimestamp(startDate),
                    to: app.my.helpers.formatTimestamp(endDate)
                }).$promise;
            })
            .then(function(value) {
                initialData.dailyOrderStats = value;
                deferred.resolve(initialData);
            })
            ["catch"](function(responseHeaders) {
                deferred.reject({status: responseHeaders.status, reason: responseHeaders.data});
            });

            return deferred.promise;
        }
    };

})();
