
angular.module('MyServices')
.factory('DailyOrder',
    ['$resource',
    function ($resource) {  // 日々の注文を扱うサービス

    var transformList = function (data, headersGetter) {
        if (data === "") {
            return [];
        }
        // utcに変換する
        var list = angular.fromJson(data);
        angular.forEach(list, function (item) {
            item.order_date = moment.utc(item.order_date);
        });
        return list;
    };

    var transformOne = function (data, headersGetter) {
        // utcに変換する
        var one = angular.fromJson(data);
        one.order_date = moment.utc(one.order_date);
        return one;
    };

    var DailyOrder = $resource('/api/daily-orders/mine/:id',
        { id: "@id" }, {
        getByOrderDate: {
            method: "GET",
            url: "/api/daily-orders/order_date/:order_date",
            params: {order_date: "@order_date"},
            isArray: true,
            transformResponse: transformList,
            cache: false
        },
        query: {
            method: "GET",
            isArray: true,
            transformResponse: transformList,
            cache: false
        },
        create: {                // 新規作成
            method: "POST",
            transformRequest: function (data, headersGetter) {
                data.order_date = data.order_date.format("YYYY-MM-DD");
                return angular.toJson(data);
            },
            transformResponse: transformOne
        },
        update: {                // 更新
            method: "PUT",
            isArray: false,
            transformRequest: function (data, headersGetter) {
                data.order_date = data.order_date.format("YYYY-MM-DD");
                return angular.toJson(data);
            },
            transformResponse: transformOne
        }
    });

    DailyOrder.prototype.total_price = function () {
        var price = 0;
        angular.forEach(this.detail_items, function (item) {
            price += item.menu_item.price_on_order;
        });
        return price;
    };

    return DailyOrder;
}]);
