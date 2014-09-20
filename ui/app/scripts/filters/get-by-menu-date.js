
angular.module('MyFilters')
.filter('getByMenuDate', function () { // menu_dateによる検索(フィルタとして定義するのが正しいのか疑問)
    return function (input, filter_date) {
        var target = null;
        input.some(function (item) {
            if (item.menu_date.valueOf() == filter_date.valueOf()) {
                target = item;
            }
            return target !== null;
        });
        return target;
    };
});
