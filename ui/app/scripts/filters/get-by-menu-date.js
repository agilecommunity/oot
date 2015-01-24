
angular.module('MyFilters')
.filter('getByMenuDate', function () { // menuDateによる検索(フィルタとして定義するのが正しいのか疑問)
    return function (input, filterDate) {
        var target = null;
        input.some(function (item) {
            if (item.menuDate.valueOf() == filterDate.valueOf()) {
                target = item;
            }
            return target !== null;
        });
        return target;
    };
});

