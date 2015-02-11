
angular.module('MyServices')
.factory('DailyOrderAggregate',
    ['$resource', '$http',
    function ($resource, $http) {  // 日々の注文を扱うサービス

    var transformResponse = {
        list: function (data, headersGetter) {
            var transformed = app.my.helpers.transformRequestDefault(data);
            if (!angular.isArray(data)) {
                return transformed;
            }
            angular.forEach(transformed, function (item) {
                item.orderDate = app.my.helpers.parseTimestamp(item.orderDate);
            });
            return transformed;
        }
    };

    var DailyOrderAggregate = $resource('/api/v1.0/daily-order-aggregates/:id',
        { id: "@id" }, {
            queryByOrderDate: {
                method: "GET",
                url: "/api/v1.0/daily-order-aggregates/order-date/:orderDate",
                isArray: true,
                transformResponse: transformResponse.list,
                cache: false
            }
    });

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