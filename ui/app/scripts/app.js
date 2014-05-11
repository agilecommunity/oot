define(['jquery',
        'angular',
        'moment',
        'angular.resource',
        'angular.route'],
        function ($,
                  angular,
                  moment) {
  "use strict";
  $(function () {
    var UserRoles = {  // ロール
        public: 1, // 001
        user: 2, // 010
        admin: 4  // 100
    };

    var AccessLevels = {  // ページのアクセスレベル
        public: UserRoles.public | // 111
            UserRoles.user |
            UserRoles.admin,
        anon: UserRoles.public,  // 001
        user: UserRoles.user |   // 110
            UserRoles.admin,
        admin: UserRoles.admin    // 100
    };

    var transform = function (data) {
        return jQuery.param(data);
    };

    angular.module('MyServices', ['ngResource', 'ngRoute'])
    .factory('User', ['$http', '$rootScope',
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
    }])
    .factory('DailyMenu', ['$resource',
                           function ($resource) {  // 日々のメニューを扱うサービス

        var transformResponse = function (data, headersGetter) {  // 結果を変換したい場合はtransformResponseを使う
            // 日付が数字でくるとDateに変換されないので、こちらで変換する
            var list = angular.fromJson(data);
            angular.forEach(list, function (item) {
                item.menu_date = moment(item.menu_date);
            });
            return list;
        };

        var DailyMenu =                                // ここだけ丁寧に解説
            $resource(                                 // RESTのAPIを簡単に扱える$resourceサービスを利用する
                '/api/daily-menus/:id',                // APIのURL。:idは変数 query,createなど必要のないときは使われない
                {id: "@id"},                           // :idを@idにマッピングする。@はオブジェクトのプロパティを意味するので、
                {                                     // DailyMenuオブジェクトのプロパティ"id"の値が使われる
                    query: {                          // queryはオブジェクト全件を取り出す
                        method: "GET",
                        isArray: true,                    // 結果が配列になる場合は必ずtrueにする(でないと、エラーが発生する)
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
    }])
    .factory('DailyOrder', ['$resource', function ($resource) {  // 日々の注文を扱うサービス

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

        var DailyOrder =
            $resource('/api/daily-orders/mine/:id',
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
    }])
    .factory('RouteFinder', ['$route', '$location',  // $routeを見つけ出すサービス
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

    var app = angular.module('oot',
            [  // アプリケーションの定義
               'ngRoute',            // 依存するサービスを指定する
               'ngResource',
               'MyServices'        // 自分が作ったサービス
            ]
    );

    app
    .controller('SigninController', ['$scope', '$location', 'User',
                                     function ($scope, $location, User) {
        $scope.signin = function () {
            User.signin($scope.user_email, $scope.user_password, {
                success: function () {
                    var path = "/order";
                    if (User.current_user().is_admin === true) { // 管理者の場合は管理インデックスに飛ばす
                        path = "/admin/index";
                    }
                    $location.path(path);
                },
                error: function (status) {
                    alert("サインインに失敗しました。 status:" + status);
                }
            });
        };
    }])
    .controller('OrderController', ['$scope', '$location', '$filter', 'User', 'DailyMenu', 'DailyOrder',
                                   function ($scope, $location, $filter, User, DailyMenu, DailyOrder) {

        $scope.daily_menus = DailyMenu.queryByStatus({status: "open"},
            function (response) { // 成功時
                   $scope.daily_orders = DailyOrder.query({},
                           function (response) { // 成功時
                           },
                           function (response) {   // 失敗時
                               alert("注文データが取得できませんでした。サインイン画面に戻ります。");
                               $location.path("/");
                           }
                   );
             },
             function (response) {   // 失敗時
                 alert("メニューのデータが取得できませんでした。サインイン画面に戻ります。");
                 $location.path("/");
             }
         );

        $scope.order = function (daily_menu, daily_menu_item) { // イベントハンドラ

            // メニューの注文状況を切り替える
            var new_state = daily_menu_item.ordered !== true;
            daily_menu_item.ordered = new_state;

            // 注文オブジェクトがあるかどうかを調べる
            var order = $filter('getByOrderDate')($scope.daily_orders, daily_menu.menu_date);

            var reload_orders = function (request) {
                // 念のためサーバから最新の注文を取得する
                $scope.daily_orders = DailyOrder.query();
            };

            if (order !== null) {
                if (new_state === true) {
                    order.detail_items.push({menu_item: daily_menu_item.menu_item});
                } else {
                    order.detail_items = order.detail_items.filter(function (item, index) {
                        return (item.menu_item.id !== daily_menu_item.menu_item.id);
                    });
                }

                // あった場合は更新する
                if (order.detail_items.length > 0) {
                    order.$update({}, reload_orders);
                } else {
                    order.$delete({}, reload_orders);
                }
            } else {
                // ない場合は新しく作る
                var new_order = new DailyOrder();
                new_order.order_date = daily_menu.menu_date.unix() * 1000;
                new_order.local_user = User.current_user();
                new_order.detail_items = [
                    {menu_item: daily_menu_item.menu_item}
                ];

                DailyOrder.create({}, [new_order], reload_orders);
            }
        };

        $scope.totalPriceOfTheDay = function (target_date) {
            var order = $filter('getByOrderDate')($scope.daily_orders, target_date);
            if (order !== null) {
                return order.total_price();
            }
            return 0;
        };

        // メニューに注文状況を反映する
        var applyOrdered = function () {
            // メニューの注文状況をリセットする
            angular.forEach($scope.daily_menus, function (menu) {
                angular.forEach(menu.detail_items, function (item) {
                    item.ordered = false;
                });
            });
            // 注文を見ながらメニューの注文状況を変更する
            angular.forEach($scope.daily_orders, function (order) {
                var menu = $filter('getByMenuDate')($scope.daily_menus, order.order_date);
                if (menu !== null) {
                    angular.forEach(order.detail_items, function (o_d_item) {
                        angular.forEach(menu.detail_items, function (m_d_item) {
                            if (o_d_item.menu_item.id === m_d_item.menu_item.id) {
                                m_d_item.ordered = true;
                            }
                        });
                    });
                }
            });
        };

        // メニュー、または注文の内容が変わった場合は、メニューの注文状況を反映しなおす
        $scope.$watchCollection("daily_menus", applyOrdered);
        $scope.$watchCollection("daily_orders", applyOrdered);

    }])
    .controller('AdminIndexController', ['$scope', '$location', '$filter', 'User', 'DailyMenu', 'DailyOrder',
                                         function ($scope, $location, $filter, User, DailyMenu, DailyOrder) {

        $scope.daily_menus = DailyMenu.query({},
            function (response) { // 成功時
                // 何もしない
            },
            function (response) {   // 失敗時
                alert("メニューのデータが取得できませんでした。サインイン画面に戻ります。");
                $location.path("/");
            }
        );

        $scope.showChecklist = function (daily_menu) {
            $location.path("/admin/checklist/menu_date/" + daily_menu.menu_date.format('YYYY-MM-DD'));
        };

    }])
    .controller('AdminChecklistController', ['$scope', '$location', '$routeParams', '$filter', 'User', 'DailyMenu', 'DailyOrder',
                                             function ($scope, $location, $routeParams, $filter, User, DailyMenu, DailyOrder) {

        // チェックリストに使うデータの作成
        var create_checklist = function () {

            var checklist = [];

            // その日注文しているユーザごとに姓名と、注文状況を調査する
            angular.forEach($scope.daily_orders, function (order) {

                var checklist_item = [];
                checklist_item.user_name = order.local_user.last_name + " " + order.local_user.first_name;
                var order_statuses = [];

                angular.forEach(order.detail_items, function (item) {
                    var order_status = [];
                    order_status.menu_id = item.menu_item.id;
                    order_status.ordered = true;
                    order_statuses[item.menu_item.id] = order_status;
                });
                checklist_item.order_statuses = order_statuses;
                checklist.push(checklist_item);
            });

            return checklist;
        };

        $scope.menu_date = moment($routeParams.menu_date);

        var param_date = $scope.menu_date.format('YYYY-MM-DD');

        $scope.daily_menu = DailyMenu.getByMenuDate({menu_date: param_date},
            function (response) {
                $scope.daily_orders = DailyOrder.getByOrderDate({order_date: param_date},
                    function (response) {
                        $scope.checklist = create_checklist();                    },
                    function (response) {
                        if (response.status === 404) {
                            return;
                        } else {
                            alert("注文のデータが取得できませんでした。");
                        }
                    });
            },
            function (response) {
                if (response.status === 404) {
                    return;
                } else {
                    alert("メニューのデータが取得できませんでした。");
                }
            }
        );

    }]);


    app
    .config(['$routeProvider', '$httpProvider',     // ルーティングの定義
            function ($routeProvider, $httpProvider) {
        $routeProvider
        .when('/', {                         // AngularJS上でのパス
            templateUrl: '/views/signin',    // 利用するビュー
            controller: 'SigninController',  // 利用するコントローラー
            access: AccessLevels.anon        // アクセス権
        })
        .when('/order', {
            templateUrl: '/views/order',
            controller: 'OrderController',
            access: AccessLevels.user
        })
        .when('/admin/index', {
            templateUrl: '/views/admin/index',
            controller: 'AdminIndexController',
            access: AccessLevels.admin
        })
        .when('/admin/checklist/menu_date/:menu_date', {
            templateUrl: '/views/admin/checklist',
            controller: 'AdminChecklistController',
            access: AccessLevels.admin,
            reloadOnSearch: false
        })
        .otherwise({                           // その他のパスが指定された場合
            redirectTo: '/'                    // "/"に飛ぶ
        });
    }]);

    app
    .filter('getByMenuDate', function () {              // menu_dateによる検索(フィルタとして定義するのが正しいのか疑問)
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
    })
    .filter('getByOrderDate', function () {         // order_dateによる検索
        return function (input, filter_date) {
            var target = null;
            input.some(function (item) {
                if (item.order_date.valueOf() == filter_date.valueOf()) {
                    target = item;
                }
                return target !== null;
            });
            return target;
        };
    })
    .filter('checkmark', function () {
        return function (input) {
            return input ? '○' : '';
        };
    });

    app.run(["$rootScope", "$location", "User", "RouteFinder",
             function ($rootScope, $location, User, RouteFinder) {

        // ブラウザのリロード対策
        $rootScope.$on('$locationChangeStart', function (ev, next, current) {

            // ルートの情報を取得
            var route = RouteFinder.parseRoute().$$route;

            // すでに認証済みの場合
            if (User.is_signed_in()) {

                // アクセスチェック
                if (User.is_accessible(route.access, User.current_user()) === true) {
                    return;
                } else {
                    alert("ページにアクセスできる権限がありません");
                    ev.preventDefault();
                    return;
                }
            }

            // 現在持っているトークンを使って再認証する
            User.re_signin({
                success: function () {
                    // 何もしない
                },
                error: function () {
                    ev.preventDefault();
                    $location.path("/");
                }
            });
        });
    }]);

    angular.bootstrap(document, ['oot']);
  });
});
