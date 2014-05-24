
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
            }
        }
    );

    return DailyMenu;
}]);
