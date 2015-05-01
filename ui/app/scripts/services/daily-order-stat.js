(function() {

    angular.module('MyServices')
        .factory('DailyOrderStat', DailyOrderStat);

    DailyOrderStat.$inject = ['$resource', '$filter'];

    function DailyOrderStat($resource, $filter) {
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

        MyClass.find = function(list, targetDate) {
            for (var i=0; i<list.length; i++) {
                if (list[i].orderDate.unix() === targetDate.unix()) {
                    return list[i];
                }
            }
            return null;
        };

        MyClass.createDummy = function() {
            var objects = [
                { orderDate: moment('2014-02-10'), numOrders: 10, numUsers: 7 },
                { orderDate: moment('2014-02-11'), numOrders: 40, numUsers: 7 }
            ];

            return objects;
        };

        MyClass.createEmptyData = function(targetDate) {
            return new MyClass({orderDate: targetDate, allStat: {}, bentoStat: {}, sideStat: {}});
        };

        return MyClass;
    }

})();
