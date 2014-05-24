
angular.module('MyFilters')
.filter('getByOrderDate', function () {         // order_dateによる検索
    return function (input, filter_date) {
        var target = null;
        input.some(function (item) {
            if (item.order_date.valueOf() == filter_date.valueOf()) {
                target = item;
            }
            return target !== null;
        });
        return target;
    };
});
