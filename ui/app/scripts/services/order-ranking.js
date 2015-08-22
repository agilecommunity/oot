(function() {

    angular.module('MyServices')
        .factory('OrderRanking', OrderRanking);

    OrderRanking.$inject = ['$resource', '$filter'];

    function OrderRanking($resource, $filter) {

        var transformResponse = {
            one: function (data, headersGetter) {
                var one = angular.fromJson(data);
                return one;
            }
        };

        var MyClass = $resource(
            '/api/v1.0/order-ranking/',
            {},
            {
                getLastMonth: {
                    method: "GET",
                    url: "/api/v1.0/order-ranking/last-month",
                    isArray: false,
                    transformResponse: transformResponse.one,
                    cache: false
                }
            }
        );

        MyClass.prototype.getResult = function(menuItem) {
            var rank = null;
            this.results.some(function (item) {
                if (menuItem.id === item.menuItem.id) {
                    rank = item;
                }
                return rank !== null;
            });

            return rank;
        };

        return MyClass;
    }

})();
