(function(){

    angular.module('MyControllers')
        .controller('AdminIndexController', AdminIndexController);

    AdminIndexController.$inject = ['$scope', '$location', 'DailyMenu', 'DailyOrderStat', 'initialData'];

    function AdminIndexController($scope, $location, DailyMenu, DailyOrderStat, initialData) {

        var vm = this;

        function createWeek(label, startDay, dailyMenus, orderStats, gatheringSetting) {

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
        }

        function toUTCDate(day) {
            return moment(day).utc().add(9, "hours").toDate();
        }

        function showMe(day) {
            $location.path("/admin/index/" + day.format('YYYY-MM-DD'));
        }

        vm.startDayOfThisWeek = moment().startOf('week').add(1, "days");
        vm.startDayOfCurrentWeek = initialData.startDayOfCurrentWeek;

        vm.gatheringSetting = initialData.gatheringSetting;

        vm.weeks = [
            createWeek("来週", initialData.startDayOfNextWeek, initialData.dailyMenus, initialData.dailyOrderStats, initialData.gatheringSetting),
            createWeek("今週", initialData.startDayOfCurrentWeek, initialData.dailyMenus, initialData.dailyOrderStats, initialData.gatheringSetting),
            createWeek("先週", initialData.startDayOfPrevWeek, initialData.dailyMenus, initialData.dailyOrderStats, initialData.gatheringSetting)
        ];

        vm.datePickerSettings = {
            currentDay: toUTCDate(initialData.startDayOfCurrentWeek),
            opened: false,
            isUnavailableDay: function(day, mode) {
                return ( mode === 'day' && ( day.getDay() !== 1 ) );
            },
            datePickerOptions: {
                showWeeks: false
            }
        };

        $scope.$watch(function(){
            return vm.datePickerSettings.currentDay;
        }, function(newVal, oldVal){
            if (oldVal.valueOf() === newVal.valueOf()) {
                return;
            }

            var localDate = moment({ year: newVal.getUTCFullYear(), month: newVal.getUTCMonth(), day: newVal.getUTCDate()});
            $location.path("/admin/index/" + localDate.format("YYYY-MM-DD"));
        });

        // カレンダーを表示する
        vm.showCalendar = function($event) {
            $event.preventDefault();
            $event.stopPropagation();

            vm.datePickerSettings.opened = true;
        };

        vm.getStatusClass = function(menu) {
            return menu === null ? "none" : menu.status;
        };

        vm.showPrevious = function() {
            showMe(moment(vm.startDayOfCurrentWeek).subtract(3, "w"));
        };

        vm.showNext = function() {
            showMe(moment(vm.startDayOfCurrentWeek).add(3, "w"));
        };

        vm.showThis = function() {
            showMe(vm.startDayOfThisWeek);
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
        initialData: function($q, $route, DailyMenu, DailyOrderStat, GatheringSetting) {

            var startDayOfCurrentWeek = moment().startOf('week').add(1, "days");
            if ($route.current.params.startDate !== undefined) {
                startDayOfCurrentWeek = moment.tz($route.current.params.startDate, moment.defaultZone.name);
            }
            var startDayOfNextWeek = moment(startDayOfCurrentWeek).add(1, "weeks");
            var startDayOfPrevWeek = moment(startDayOfCurrentWeek).add(-1, "weeks");

            var deferred = $q.defer();
            var initialData = {};

            initialData.startDayOfCurrentWeek = startDayOfCurrentWeek;
            initialData.startDayOfNextWeek = startDayOfNextWeek;
            initialData.startDayOfPrevWeek = startDayOfPrevWeek;

            var startDate = startDayOfPrevWeek;
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
