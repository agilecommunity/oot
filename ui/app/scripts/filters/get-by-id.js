
angular.module('MyFilters')
.filter('getById', function () { // IDによる検索(フィルタとして定義するのが正しいのか疑問)
    return function (input, id) {
        var target = null;
        input.some(function (item) {
            if (item.id === id) {
                target = item;
            }
            return target !== null;
        });
        return target;
    };
});

