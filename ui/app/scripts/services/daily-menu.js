(function(){

    angular.module('MyServices')
        .factory('DailyMenu', DailyMenu);

    DailyMenu.$inject = ['$resource', '$filter'];

    var MenuStatuses = {
        "prepared": "準備中",
        "open": "受付中",
        "closed": "終了"
    };

    function DailyMenu($resource, $filter) {

        var transformList = function (data, headersGetter) {  // 結果を変換したい場合はtransformResponseを使う
            var list = angular.fromJson(data);
            angular.forEach(list, function (item) {
                item.menuDate = app.my.helpers.parseTimestamp(item.menuDate);
            });
            return list;
        };

        var transformOne = function (data, headersGetter) {
            if ($filter('isEmptyOrUndefined')(data)) {
                return null;
            }
            var one = angular.fromJson(data);
            one.menuDate = app.my.helpers.parseTimestamp(one.menuDate);
            return one;
        };

        var MyClass = $resource(                 // RESTのAPIを簡単に扱える$resourceサービスを利用する
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
                    isArray: true,
                    transformResponse: transformList
                },
                getByMenuDate: {
                    method: "GET",
                    url: "/api/v1.0/daily-menus/menu-date/:menuDate",
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

        MyClass.prototype.statusText = function() {
            return MenuStatuses[this.status];
        };

        MyClass.prototype.isEmpty = function() {
            return this.detailItems.length === 0;
        };

        MyClass.find = function(list, menuDate) {
            for (var i=0; i<list.length; i++) {
                if (list[i].menuDate.unix() === menuDate.unix()) {
                    return list[i];
                }
            }
            return null;
        };

        // メニューのリストから該当の日付のメニューを探し、そのindexを返す
        MyClass.findIndexByMenuDate = function(list, menuDate) {
            for (var i=0; i<list.length; i++) {
                if (list[i].menuDate.unix() === menuDate.unix()) {
                    return i;
                }
            }
            return -1;
        };

        // 空のデータを作成する
        MyClass.createEmptyData = function(targetDate) {
            return new MyClass({menuDate: targetDate, detailItems: []});
        };

        return MyClass;
    }

})();
