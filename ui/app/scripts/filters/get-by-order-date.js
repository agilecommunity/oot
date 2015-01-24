
angular.module('MyFilters')
.filter('getByOrderDate', function () {         // order_dateによる検索
    return function (input, filterDate) {
        var target = null;
        input.some(function (item) {
            if (item.orderDate.valueOf() == filterDate.valueOf()) {
                target = item;
            }
            return target !== null;
        });
        return target;
    };
});
