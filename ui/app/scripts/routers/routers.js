(function (app) {
    "use strict";

    var UserRoles = app.UserRoles;
    var AccessLevels = app.AccessLevels;

    return app.config(
        ['$routeProvider', '$httpProvider',     // ルーティングの定義
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
        .when('/admin/daily-menus/new', {
            templateUrl: '/views/admin/daily-menu/new',
            controller: 'DailyMenuNewController',
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
})(window.app);