
angular.module('MyServices')
.factory('DailyOrder',
    ['$resource',
    function ($resource) {  // 日々の注文を扱うサービス

    var transformRequest = {
        one: function (data, headersGetter) {
            data.orderDate = app.my.helpers.formatTimestamp(data.orderDate);
            return angular.toJson(data);
        }
    };

    var transformResponse = {
        list: function (data, headersGetter) {
            if (data === "") {
                return [];
            }
            var list = angular.fromJson(data);
            angular.forEach(list, function (item) {
                item.orderDate = app.my.helpers.parseTimestamp(item.orderDate);
            });
            return list;
        },
        one: function (data, headersGetter) {
            var one = angular.fromJson(data);
            one.orderDate = app.my.helpers.parseTimestamp(one.orderDate);
            return one;
        }
    };

    var DailyOrder = $resource('/api/v1.0/daily-orders/:id',
        { id: "@id" }, {
        query: {
            method: "GET",
            isArray: true,
            transformResponse: transformResponse.list,
            cache: false
        },
        queryByOrderDate: {
            method: "GET",
            url: "/api/v1.0/daily-orders/order-date/:orderDate",
            isArray: true,
            transformResponse: transformResponse.list,
            cache: false
        },
        queryMine: {
            method: "GET",
            url: "/api/v1.0/daily-orders/mine",
            isArray: true,
            transformResponse: transformResponse.list,
            cache: false
        },
        create: {                // 新規作成
            method: "POST",
            transformRequest: transformRequest.one,
            transformResponse: transformResponse.one
        },
        update: {                // 更新
            method: "PUT",
            isArray: false,
            transformRequest: transformRequest.one,
            transformResponse: transformResponse.one
        }
    });

    DailyOrder.prototype.totalReducedOnOrder = function () {
        var price = 0;
        angular.forEach(this.detailItems, function (item) {
            price += item.menuItem.reducedOnOrder * item.numOrders;
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

    // メニューのリストから該当の日付のメニューを探し、そのindexを返す
    DailyOrder.filterByOrderDate = function(list, targetDate) {
        var results = [];
        for (var i=0; i<list.length; i++) {
            if (list[i].orderDate.unix() === targetDate.unix()) {
                results.push(list[i]);
            }
        }
        return results;
    };

    return DailyOrder;
}]);
