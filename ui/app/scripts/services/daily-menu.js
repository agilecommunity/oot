
angular.module('MyServices')
.factory('DailyMenu',
    ['$resource',
    function ($resource) {  // 日々のメニューを扱うサービス

    var transformResponse = function (data, headersGetter) {  // 結果を変換したい場合はtransformResponseを使う
        // 日付が数字でくるとDateに変換されないので、こちらで変換する
        var list = angular.fromJson(data);
        angular.forEach(list, function (item) {
            item.menu_date = moment(item.menu_date);
        });
        return list;
    };

    var DailyMenu = $resource(                 // RESTのAPIを簡単に扱える$resourceサービスを利用する
        '/api/daily-menus/:id',                // APIのURL。:idは変数 query,createなど必要のないときは使われない
        {id: "@id"},                           // :idを@idにマッピングする。@はオブジェクトのプロパティを意味するので、
        {                                      // DailyMenuオブジェクトのプロパティ"id"の値が使われる
            query: {                           // queryはオブジェクト全件を取り出す
                method: "GET",
                isArray: true,                 // 結果が配列になる場合は必ずtrueにする(でないと、エラーが発生する)
                transformResponse: transformResponse,
                cache: false
            },
            queryByStatus: {
                method: "GET",
                url: "/api/daily-menus/status/:status",
                params: {status: "@status"},
                isArray: true,
                transformResponse: transformResponse
            },
            getByMenuDate: {
                method: "GET",
                url: "/api/daily-menus/menu_date/:menu_date",
                params: {menu_date: "@menu_date"},
                isArray: false,
                cache: false
            },
            create: {
                method: "POST"
            },
            update: {
                method: "PUT",
               isArray: false
            }
        }
    );

    // メニューから該当するItemを探し、そのindexを返す
    DailyMenu.prototype.findMenuItem = function(menu_item) {
        // jQuery in Arrayよりコピペ
        var len;
        var arr = this.detail_items;

        if (arr === undefined || arr === null) {
            return -1;
        }

        len = arr.length;
        var i = i ? i < 0 ? Math.max( 0, len + i ) : i : 0;

        for ( ; i < len; i++ ) {
            // Skip accessing in sparse arrays
            if ( i in arr && arr[ i ].menu_item.id === menu_item.id ) {
                return i;
            }
        }

        return -1;
    };

    // メニューのリストから該当の日付のメニューを探し、そのindexを返す
    DailyMenu.findByMenuDate = function(list, menu_date) {
        for (var i=0; i<list.length; i++) {
            if (list[i].menu_date.unix() === menu_date.unix()) {
                return i;
            }
        }
        return -1;
    };


    return DailyMenu;
}]);
