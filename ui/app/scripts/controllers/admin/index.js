(function(){

    angular.module('MyControllers')
        .controller('AdminIndexController', AdminIndexController);

    AdminIndexController.$inject = ['$location', 'DailyMenu', 'DailyOrderStat', 'initialData'];

    function AdminIndexController($location, DailyMenu, DailyOrderStat, initialData) {

        var vm = this;

        var createWeek = function(label, startDay, dailyMenus, orderStats, gatheringSetting) {

            var days = [];
            for(var index=0; index<5; index++) {
                var currentDay = moment(startDay).add(index, "days");
                var currentMenu = DailyMenu.find(dailyMenus, currentDay);
                var currentStatus = currentMenu === null ? "未作成" : currentMenu.statusText();
                var currentOrderStat = DailyOrderStat.find(orderStats, currentDay);

                if (currentOrderStat === null) {
                    currentOrderStat = DailyOrderStat.createEmptyData(currentDay);
                }

                var gatheringStatus = gatheringSetting.isAchieved(currentOrderStat.bentoStat.numOrders) ? "達成" : "未達成";
                if (currentMenu === null) {
                    gatheringStatus = "";
                }

                days.push({
                    day: currentDay,
                    menu: currentMenu,
                    status: currentStatus,
                    orderStat: currentOrderStat,
                    gatheringStatus: gatheringStatus
                });
            }

            return {
                label: label,
                startDay: startDay,
                endDay: _.last(days).day,
                days: days
            };
        };

        vm.gatheringSetting = initialData.gatheringSetting;

        vm.weeks = [
            createWeek("来週", initialData.startDayOfNextWeek, initialData.dailyMenus, initialData.dailyOrderStats, initialData.gatheringSetting),
            createWeek("今週", initialData.startDayOfThisWeek, initialData.dailyMenus, initialData.dailyOrderStats, initialData.gatheringSetting),
            createWeek("先週", initialData.startDayOfLastWeek, initialData.dailyMenus, initialData.dailyOrderStats, initialData.gatheringSetting)
        ];

        vm.getStatusClass = function(menu) {
            return menu === null ? "none" : menu.status;
        };

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

        vm.editDailyMenu = function (targetDay) {
            $location.path("/admin/daily-menus/" + targetDay.format('YYYY-MM-DD'));
        };

    }

    app.my.resolvers.AdminIndexController = {
        initialData: function($q, DailyMenu, DailyOrderStat, GatheringSetting) {

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

                return GatheringSetting.get({
                }).$promise;
            })
            .then(function(value) {
                initialData.gatheringSetting = value;

                deferred.resolve(initialData);
            })
            ["catch"](function(responseHeaders) {
                deferred.reject(responseHeaders);
            });

            return deferred.promise;
        }
    };

})();
