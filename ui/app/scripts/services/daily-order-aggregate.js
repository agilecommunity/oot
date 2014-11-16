
angular.module('MyServices')
    .factory('DailyOrderAggregate',
    ['$resource',
    function ($resource) {  // 日々の注文を扱うサービス

    var transformList = function (data, headersGetter) {
        // utcに変換する
        var list = angular.fromJson(data);
        angular.forEach(list, function (item) {
            if (item.code === undefined || item.code === null || item.code === ""){
                item.code = "(空)";
            }
            item.orderDate = moment.utc(item.orderDate);
        });
        return list;
    };

    var DailyOrderAggregate = $resource('/api/v1.0/daily-order-aggregates/:id',
        { id: "@id" }, {
            getByOrderDate: {
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
