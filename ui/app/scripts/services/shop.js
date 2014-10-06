
angular.module('MyServices')
    .factory('Shop',
    ['$resource',
    function ($resource) {  // メニューにある弁当、サラダ

        var Shop = $resource('/api/shops/:id',
            { id: "@id" }, {
                query: {
                    method: "GET",
                    isArray: true,
                    cache: false
                }
            });

        return Shop;
}]);
