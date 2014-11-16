
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

    var DailyOrder = $resource('/api/daily-orders/:id',
        { id: "@id" }, {
        getByOrderDate: {
            method: "GET",
            url: "/api/daily-orders/order_date/:order_date",
            params: {order_date: "@order_date"},
            isArray: true,
            transformResponse: transformList,
            cache: false
        },
        getMine: {
            method: "GET",
            url: "/api/daily-orders/mine",
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
            price += item.menu_item.price_on_order * item.num_orders;
        });
        return price;
    };

    DailyOrder.prototype.find_item = function(menu_item) {
        var target = null;
        this.detail_items.some(function (item) {
            if (item.menu_item.id == menu_item.id) {
                target = item;
            }
            return target !== null;
        });
        return target;
    };

    DailyOrder.prototype.remove_item = function(menu_item) {
        this.detail_items = this.detail_items.filter(function (item, index) {
            return (item.menu_item.id !== menu_item.id);
        });
    };

    DailyOrder.prototype.add_item = function(menu_item, num_orders) {
        if (this.find_item(menu_item) !== null) {
            return;
        }

        this.detail_items.push({menu_item: menu_item, num_orders: num_orders});
    };

    return DailyOrder;
}]);
