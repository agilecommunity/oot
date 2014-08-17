
angular.module('MyServices')
.factory('MenuItem',
    ['$resource',
    function ($resource) {  // メニューにある弁当、サラダ

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

    var MenuItem = $resource('/api/menu-items/:id',
        { id: "@id" }, {
        query: {
            method: "GET",
            isArray: true,
            transformResponse: transformResponse,
            cache: false
        }
    });

    return MenuItem;
}]);
