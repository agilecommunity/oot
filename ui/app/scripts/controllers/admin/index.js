
angular.module('MyControllers')
.controller('AdminIndexController',
    ['$scope', '$location', '$filter', 'User',
    function ($scope, $location, $filter, User) {

    var createWeek = function(label, startDay) {

        var days = [];
        for(var index=0; index<5; index++) {
            days.push(moment(startDay).add(index, "days"));
        }

        return {
            label: label,
            startDay: startDay,
            endDay: _.last(days),
            days: days
        };
    };

    var setUpWeeks = function() {
        var startDayOfThisWeek = moment().startOf('week').add(1, "days");
        var startDayOfNextWeek = moment(startDayOfThisWeek).add(1, "weeks");
        var startDayOfLastWeek = moment(startDayOfThisWeek).add(-1, "weeks");

        $scope.weeks = [
            createWeek("来週", startDayOfNextWeek),
            createWeek("今週", startDayOfThisWeek),
            createWeek("先週", startDayOfLastWeek)
        ];
    };

    var setUp = function() {
        setUpWeeks();
    };

    setUp();

    $scope.showPurchaseOrderConfirmation = function (targetDay) {
        $location.path("/admin/purchase-order/" + targetDay.format('YYYY-MM-DD') + "/confirmation");
    };

    $scope.showChecklist = function (targetDay) {
        $location.path("/admin/checklist/menu-date/" + targetDay.format('YYYY-MM-DD'));
    };

}]);

