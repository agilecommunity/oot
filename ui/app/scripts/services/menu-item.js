
angular.module('MyServices')
.factory('MenuItem',
    ['$resource',
    function ($resource) {  // メニューにある弁当、サラダ

    var transformResponse = {
        one: function (data, headersGetter) {
            if (data === "") {
                return [];
            }
            var list = angular.fromJson(data);
            return list;
        }
    };

    var MenuItem = $resource('/api/v1.0/menu-items/:id',
        { id: "@id" }, {
        query: {
            method: "GET",
            isArray: true,
            transformResponse: transformResponse.one,
            cache: false
        },
        queryByShopName: {
            url: "/api/v1.0/shops/:shopName/menu-items",
            method: "GET",
            isArray: true,
            transformResponse: transformResponse.one,
            cache: false
        },
        create: {                // 新規作成
            method: "POST",
            isArray: true
        },
        update: {                // 更新
            method: "PUT",
            isArray: false
        }
    });

    return MenuItem;
}]);
