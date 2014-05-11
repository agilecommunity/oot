define(['jquery',
        'angular',
        'moment',
        'constants/access_levels',
        'angular.resource',
        'angular.route'],
function ($,
          angular,
          moment,
          AccessLevels) {
    "use strict";

    var services = angular.module('MyServices', ['ngResource', 'ngRoute']);

    var transform = function (data) {
        return jQuery.param(data);
    };

    services.factory('User',
        ['$http', '$rootScope',
        function ($http, $rootScope) {  // ユーザ認証を行うサービス

        $rootScope.current_user = null;

        var User = {
            current_user: function () {
                return $rootScope.current_user;
            },
            is_accessible: function (access, user) {
                // 誰でもアクセス可能な場合は、true
                if (access === AccessLevels.anon || access === AccessLevels.public) {
                    return true;
                }

                // サインインが必要な場合は、userオブジェクトがnullでなければOK
                if (access === AccessLevels.user && user !== null) {
                    return true;
                }

                if (access === AccessLevels.admin && user !== null && user.is_admin === true) {
                    return true;
                }

                return false;
            },
            is_signed_in: function () {
                return ($rootScope.current_user !== null);
            },

              // ユーザ名、パスワードで認証を行う
              signin: function (username, password, callback) {
                  var parameter = {};
                  parameter.username = username;
                  parameter.password = password;

                  $http({
                      method: 'POST',
                      url: '/authenticate/userpass',
                      headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' },
                      transformRequest: transform,
                      data: parameter
                  })
                      .success(function (data, status, header) {
                          $rootScope.current_user = data;
                          callback.success();
                      })
                      .error(function (data, status, header) {
                          $rootScope.current_user = null;
                          callback.error(status);
                      });
              },

              // 取得しているトークンで認証情報を所得してみる
              re_signin: function (callback) {

                  $http.get("/api/users/me")
                      .success(function (data, status, header) {
                          $rootScope.current_user = data;
                          callback.success();
                      })
                      .error(function (data, status, header) {
                          $rootScope.current_user = null;
                          callback.error();
                      });
              }
          };

          return User;
    }]);

    services.factory('DailyMenu',
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

    services.factory('DailyOrder',
        ['$resource',
        function ($resource) {  // 日々の注文を扱うサービス

        var transformResponse = function (data, headersGetter) {
            if (data === "") {
                return [];
            }

            // 日付が数字でくるとDateに変換されないので、こちらで変換する
            var list = angular.fromJson(data);
            angular.forEach(list, function (item) {
                item.order_date = moment(item.order_date);
            });
            return list;
        };

        var DailyOrder = $resource('/api/daily-orders/mine/:id',
            { id: "@id" }, {
            getByOrderDate: {
                method: "GET",
                url: "/api/daily-orders/order_date/:order_date",
                params: {order_date: "@order_date"},
                isArray: true,
                transformResponse: transformResponse,
                cache: false
            },
            query: {
                method: "GET",
                isArray: true,
                transformResponse: transformResponse,
                cache: false
            },
            create: {                // 新規作成
                method: "POST"
            },
            update: {                // 更新
                method: "PUT",
                isArray: false,
                transformRequest: function (data, headersGetter) {
                    // 日付を数値に変換する
                    data.order_date = data.order_date.unix() * 1000;
                    return angular.toJson(data);
                }
            }
        });

        DailyOrder.prototype.total_price = function () {
            var price = 0;
            angular.forEach(this.detail_items, function (item) {
                price += item.menu_item.price_on_order;
            });
            return price;
        };

        return DailyOrder;
    }]);

    services.factory('RouteFinder',
        ['$route', '$location',         // $routeを見つけ出すサービス
        function ($route, $location) {  // angular-route.jsからコピーしたもの

        var inherit = function (parent, extra) {
            return angular.extend(new (angular.extend(function () {
            }, {prototype: parent}))(), extra);
              };

              /**
               * @param on {string} current url
               * @param route {Object} route regexp to match the url against
               * @return {?Object}
               *
               * @description
               * Check if the route matches the current url.
               *
               * Inspired by match in
               * visionmedia/express/lib/router/router.js.
               */
              var switchRouteMatcher = function (on, route) {
                  var keys = route.keys,
                      params = {};

                  if (!route.regexp) return null;

                  var m = route.regexp.exec(on);
                  if (!m) return null;

                  for (var i = 1, len = m.length; i < len; ++i) {
                      var key = keys[i - 1];

                      var val = 'string' == typeof m[i] ?
                          decodeURIComponent(m[i])
                          : m[i];

                      if (key && val) {
                          params[key.name] = val;
                      }
                  }
                  return params;
              };

              return {
                  /**
                   * @returns {Object} the current active route, by matching it against the URL
                   */
                  parseRoute: function () {
                      // Match a route
                      var params, match;
                      angular.forEach($route.routes, function (route, path) {
                          if (!match && (params = switchRouteMatcher($location.path(), route))) {
                              match = inherit(route, {
                                  params: angular.extend({}, $location.search(), params),
                                  pathParams: params});
                              match.$$route = route;
                          }
                      });
                      // No route matched; fallback to "otherwise" route
                      return match || routes[null] && inherit(routes[null], {params: {}, pathParams: {}});
                  }
              };
    }]);

    return services;
});
