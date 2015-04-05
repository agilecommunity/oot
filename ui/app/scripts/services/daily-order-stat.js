(function() {

    angular.module('MyServices')
        .factory('DailyOrderStat', DailyOrderStat);

    DailyOrderStat.$inject = ['$resource', '$filter'];

    var transformList = function (data, headersGetter) {
        var list = angular.fromJson(data);
        angular.forEach(list, function (item) {
            item.orderDate = app.my.helpers.parseTimestamp(item.orderDate);
        });
        return list;
    };

    var transformOne = function (data, headersGetter) {
        if ($filter('isEmptyOrUndefined')(data)) {
            return null;
        }
        var one = angular.fromJson(data);
        one.orderDate = app.my.helpers.parseTimestamp(one.orderDate);
        return one;
    };

    function DailyOrderStat($resource, $filter) {
        var MyClass = $resource(
            '/api/v1.0/daily-order-stats/:id',
            {id: "@id"},
            {
                query: {
                    method: "GET",
                    isArray: true,
                    transformResponse: transformList,
                    cache: false
                }
            }
        );

        MyClass.createDummy = function() {
            var objects = [
                { orderDate: moment('2014-02-10'), numOrders: 10, numUsers: 7 },
                { orderDate: moment('2014-02-11'), numOrders: 40, numUsers: 7 }
            ];

            return objects;
        };

        return MyClass;
    }

})();
