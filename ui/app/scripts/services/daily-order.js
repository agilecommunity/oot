
angular.module('MyServices')
.factory('DailyOrder',
    ['$resource',
    function ($resource) {  // 日々の注文を扱うサービス

    var transformList = function (data, headersGetter) {
        if (data === "") {
            return [];
        }
        var list = angular.fromJson(data);
        angular.forEach(list, function (item) {
            item.orderDate = app.my.helpers.parseDate(item.orderDate);
        });
        return list;
    };

    var transformOne = function (data, headersGetter) {
        var one = angular.fromJson(data);
        one.orderDate = app.my.helpers.parseDate(one.orderDate);
        return one;
    };

    var DailyOrder = $resource('/api/v1.0/daily-orders/:id',
        { id: "@id" }, {
        query: {
            method: "GET",
            isArray: true,
            transformResponse: transformList,
            cache: false
        },
        queryByOrderDate: {
            method: "GET",
            url: "/api/v1.0/daily-orders/order-date/:orderDate",
            params: {orderDate: "@orderDate"},
            isArray: true,
            transformResponse: transformList,
            cache: false
        },
        queryMine: {
            method: "GET",
            url: "/api/v1.0/daily-orders/mine",
            isArray: true,
            transformResponse: transformList,
            cache: false
        },
        create: {                // 新規作成
            method: "POST",
            transformRequest: function (data, headersGetter) {
                data.orderDate = data.orderDate.format("YYYY-MM-DDZ");
                return angular.toJson(data);
            },
            transformResponse: transformOne
        },
        update: {                // 更新
            method: "PUT",
            isArray: false,
            transformRequest: function (data, headersGetter) {
                data.orderDate = data.orderDate.format("YYYY-MM-DDZ");
                return angular.toJson(data);
            },
            transformResponse: transformOne
        }
    });

    DailyOrder.prototype.totalPrice = function () {
        var price = 0;
        angular.forEach(this.detailItems, function (item) {
            price += item.menuItem.priceOnOrder * item.numOrders;
        });
        return price;
    };

    DailyOrder.prototype.findItem = function(menuItem) {
        var target = null;
        this.detailItems.some(function (item) {
            if (item.menuItem.id == menuItem.id) {
                target = item;
            }
            return target !== null;
        });
        return target;
    };

    DailyOrder.prototype.removeItem = function(menuItem) {
        this.detailItems = this.detailItems.filter(function (item, index) {
            return (item.menuItem.id !== menuItem.id);
        });
    };

    DailyOrder.prototype.addItem = function(menuItem, numOrders) {
        if (this.findItem(menuItem) !== null) {
            return;
        }

        this.detailItems.push({menuItem: menuItem, numOrders: numOrders});
    };

    DailyOrder.prototype.$save = function(params, success, error) {
        if ( !this.id ) {
            return this.$create(params, success, error);
        }
        else {
            return this.$update(params, success, error);
        }
    };

    return DailyOrder;
}]);
