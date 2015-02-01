
angular.module('MyServices')
.factory('DailyOrderAggregate',
    ['$resource', '$http',
    function ($resource, $http) {  // 日々の注文を扱うサービス

    var transformList = function (data, headersGetter) {

        var transformed = app.my.helpers.transformRequestDefault(data);

        if (!angular.isArray(data)) {
            return transformed;
        }

        angular.forEach(transformed, function (item) {
            item.orderDate = app.my.helpers.parseDate(item.orderDate);
        });
        return transformed;
    };

    var DailyOrderAggregate = $resource('/api/v1.0/daily-order-aggregates/:id',
        { id: "@id" }, {
            queryByOrderDate: {
                method: "GET",
                url: "/api/v1.0/daily-order-aggregates/order-date/:orderDate",
                params: {orderDate: "@orderDate"},
                isArray: true,
                transformResponse: transformList,
                cache: false
            }
    });

    return DailyOrderAggregate;
}]);