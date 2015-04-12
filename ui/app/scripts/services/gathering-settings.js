(function() {

    angular.module('MyServices')
        .factory('GatheringSetting', GatheringSetting);

    GatheringSetting.$inject = ['$resource', '$filter'];

    function GatheringSetting($resource, $filter) {

        var transformRequest = {
            one: function (data, headersGetter) {
                data.createdAt = app.my.helpers.parseTimestamp(data.createdAt);
                data.updatedAt = app.my.helpers.parseTimestamp(data.updatedAt);
                return angular.toJson(data);
            }
        };

        var transformResponse = {
            one: function (data, headersGetter) {
                var one = angular.fromJson(data);
                one.createdAt = app.my.helpers.parseTimestamp(one.createdAt);
                one.updatedAt = app.my.helpers.parseTimestamp(one.updatedAt);
                return one;
            }
        };

        var transformOne = function (data, headersGetter) {
            if ($filter('isEmptyOrUndefined')(data)) {
                return null;
            }
            var one = angular.fromJson(data);
            return one;
        };

        var MyClass = $resource(
            '/api/v1.0/settings/gathering/:id',
            {id: "@id"},
            {
                get: {
                    method: "GET",
                    url: "/api/v1.0/settings/gathering",
                    isArray: false,
                    cache: false,
                    transformResponse: transformResponse.one
                },
                update: {                // 更新
                    method: "PUT",
                    url: "/api/v1.0/settings/gathering",
                    isArray: false,
                    transformRequest: transformRequest.one,
                    transformResponse: transformResponse.one
                }
            }
        );

        MyClass.prototype.isAchieved = function(numOrders) {
            return (numOrders >= this.minOrders);
        };

        MyClass.createDummy = function() {
            var object = new MyClass();

            object.enabled = true;
            object.minOrders = 30;
            object.discountPrice = 20;

            return object;
        };

        return MyClass;
    }

})();
