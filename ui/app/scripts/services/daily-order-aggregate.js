
angular.module('MyServices')
    .factory('DailyOrderAggregate',
    ['$resource',
    function ($resource) {  // 日々の注文を扱うサービス

    var transformList = function (data, headersGetter) {
        // utcに変換する
        var list = angular.fromJson(data);
        angular.forEach(list, function (item) {
            item.order_date = moment.utc(item.order_date);
        });
        return list;
    };

    var DailyOrderAggregate = $resource('/api/daily-order-aggregates/:id',
        { id: "@id" }, {
            getByOrderDate: {
                method: "GET",
                url: "/api/daily-order-aggregates/order_date/:order_date",
                params: {order_date: "@order_date"},
                isArray: true,
                transformResponse: transformList,
                cache: false
            }
    });

    return DailyOrderAggregate;
}]);
