
angular.module('MyServices')
.factory('DailyOrder',
    ['$resource',
    function ($resource) {  // 日々の注文を扱うサービス

    var transformResponse = function (data, headersGetter) {
        if (data === "") {
            return [];
        }

        // 日付が数字でくるとDateに変換されないので、こちらで変換する
        var list = angular.fromJson(data);
        angular.forEach(list, function (item) {
            item.order_date = moment(item.order_date);
        });
        return list;
    };

    var DailyOrder = $resource('/api/daily-orders/mine/:id',
        { id: "@id" }, {
        getByOrderDate: {
            method: "GET",
            url: "/api/daily-orders/order_date/:order_date",
            params: {order_date: "@order_date"},
            isArray: true,
            transformResponse: transformResponse,
            cache: false
        },
        query: {
            method: "GET",
            isArray: true,
            transformResponse: transformResponse,
            cache: false
        },
        create: {                // 新規作成
            method: "POST"
        },
        update: {                // 更新
            method: "PUT",
            isArray: false,
            transformRequest: function (data, headersGetter) {
                // 日付を数値に変換する
                data.order_date = data.order_date.unix() * 1000;
                return angular.toJson(data);
            }
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
