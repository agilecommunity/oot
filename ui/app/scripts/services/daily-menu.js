
angular.module('MyServices')
.factory('DailyMenu',
    ['$resource',
    function ($resource) {  // 日々のメニューを扱うサービス

    var transformList = function (data, headersGetter) {  // 結果を変換したい場合はtransformResponseを使う
        // utcに変換する
        var list = angular.fromJson(data);
        angular.forEach(list, function (item) {
            item.menuDate = moment.utc(item.menuDate);
        });
        return list;
    };

    var transformOne = function (data, headersGetter) {
        // utcに変換する
        var one = angular.fromJson(data);
        one.menuDate = moment.utc(one.menuDate);
        return one;
    };

    var DailyMenu = $resource(                 // RESTのAPIを簡単に扱える$resourceサービスを利用する
        '/api/v1.0/daily-menus/:id',           // APIのURL。:idは変数 query,createなど必要のないときは使われない
        {id: "@id"},                           // :idを@idにマッピングする。@はオブジェクトのプロパティを意味するので、
        {                                      // DailyMenuオブジェクトのプロパティ"id"の値が使われる
            query: {                           // queryはオブジェクト全件を取り出す
                method: "GET",
                isArray: true,                 // 結果が配列になる場合は必ずtrueにする(でないと、エラーが発生する)
                transformResponse: transformList,
                cache: false
            },
            queryByStatus: {
                method: "GET",
                url: "/api/v1.0/daily-menus/status/:status",
                params: {status: "@status"},
                isArray: true,
                transformResponse: transformList
            },
            getByMenuDate: {
                method: "GET",
                url: "/api/v1.0/daily-menus/menu-date/:menuDate",
                params: {menuDate: "@menuDate"},
                isArray: false,
                cache: false,
                transformResponse: transformOne
            },
            create: {
                method: "POST",
                transformResponse: transformOne
            },
            update: {
                method: "PUT",
               isArray: false,
                transformResponse: transformOne
            }
        }
    );

    // メニューから該当するItemを探し、そのindexを返す
    DailyMenu.prototype.findMenuItem = function(menuItem) {
        // jQuery in Arrayよりコピペ
        var len;
        var arr = this.detailItems;

        if (arr === undefined || arr === null) {
            return -1;
        }

        len = arr.length;
        var i = i ? i < 0 ? Math.max( 0, len + i ) : i : 0;

        for ( ; i < len; i++ ) {
            // Skip accessing in sparse arrays
            if ( i in arr && arr[ i ].menuItem.id === menuItem.id ) {
                return i;
            }
        }

        return -1;
    };

    // メニューのリストから該当の日付のメニューを探し、そのindexを返す
    DailyMenu.findByMenuDate = function(list, menuDate) {
        for (var i=0; i<list.length; i++) {
            if (list[i].menuDate.unix() === menuDate.unix()) {
                return i;
            }
        }
        return -1;
    };

    return DailyMenu;
}]);
