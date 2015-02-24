
angular.module('MyServices')
.factory('DailyOrderAggregate',
    ['$resource', '$http',
    function ($resource, $http) {  // 日々の注文を扱うサービス

    var transformResponse = {
        list: function (data, headersGetter) {
            var transformed = app.my.helpers.transformRequestDefault(data);
            if (!angular.isArray(transformed)) {
                return transformed;
            }
            angular.forEach(transformed, function (item) {
                item.orderDate = app.my.helpers.parseTimestamp(item.orderDate);
                console.log(item.orderDate);
            });
            return transformed;
        }
    };

    var DailyOrderAggregate = $resource('/api/v1.0/daily-order-aggregates/:id',
        { id: "@id" }, {
            query: {
                method: "GET",
                isArray: true,
                transformResponse: transformResponse.list,
                cache: false
            },
            queryByOrderDate: {
                method: "GET",
                url: "/api/v1.0/daily-order-aggregates/order-date/:orderDate",
                isArray: true,
                transformResponse: transformResponse.list,
                cache: false
            }
    });

    // メニューのリストから該当のメニューを探し、返す
    DailyOrderAggregate.find = function(list, targetDate, menuItem) {
        for (var i=0; i<list.length; i++) {
            if (list[i].orderDate.valueOf() === targetDate.valueOf() && list[i].menuItemId === menuItem.id) {
                return list[i];
            }
        }
        return null;
    };

    // メニューのリストから該当のメニューを探し、そのindexを返す
    DailyOrderAggregate.findByMenuItem = function(list, menuItem) {
        for (var i=0; i<list.length; i++) {
            if (list[i].menuItemId === menuItem.id) {
                return i;
            }
        }
        return -1;
    };

    return DailyOrderAggregate;
}]);