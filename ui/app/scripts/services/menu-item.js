
angular.module('MyServices')
.factory('MenuItem',
    ['$resource',
    function ($resource) {  // メニューにある弁当、サラダ

    var transformResponse = function (data, headersGetter) {
        if (data === "") {
            return [];
        }
        var list = angular.fromJson(data);
        return list;
    };

    var MenuItem = $resource('/api/menu-items/:id',
        { id: "@id" }, {
        query: {
            method: "GET",
            isArray: true,
            transformResponse: transformResponse,
            cache: false
        },
        queryByShopName: {
            url: "/api/shops/:shop_name/menu-items",
            method: "GET",
            isArray: true,
            transformResponse: transformResponse,
            cache: false
        },
        create: {                // 新規作成
            method: "POST",
            isArray: false
        },
        update: {                // 更新
            method: "PUT",
            isArray: false
        }
    });

    MenuItem.prototype.$save = function(params, success, error) {
        if ( !this.id ) {
            return this.$create(params, success, error);
        }
        else {
            return this.$update(params, success, error);
        }
    };

    return MenuItem;
}]);
