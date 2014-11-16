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
        .when('/signup', {
            templateUrl: '/views/start-signup',
            controller: 'StartSignupController',
            access: AccessLevels.anon
        })
        .when('/signup/:token', {
            templateUrl: '/views/signup',
            controller: 'SignupController',
            access: AccessLevels.anon
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
        .when('/admin/order-aggregates/order-date/:orderDate', {
            templateUrl: '/views/admin/order-aggregates',
            controller: 'AdminOrderAggregatesController',
            access: AccessLevels.admin,
            reloadOnSearch: false
        })
        .when('/admin/checklist/menu-date/:menuDate', {
            templateUrl: '/views/admin/checklist',
            controller: 'AdminChecklistController',
            access: AccessLevels.admin,
            reloadOnSearch: false
        })
        .when('/admin/menu-items/index', {
            templateUrl: '/views/admin/menu-item/index',
            controller: 'MenuItemIndexController',
            access: AccessLevels.admin
        })
        .when('/admin/menu-items/import', {
            templateUrl: '/views/admin/menu-item/import',
            controller: 'MenuItemImportController',
            access: AccessLevels.admin
        })
        .when('/admin/menu-item-images/import', {
            templateUrl: '/views/admin/menu-item-image/import',
            controller: 'MenuItemImageImportController',
            access: AccessLevels.admin
        })
        .otherwise({                           // その他のパスが指定された場合
            redirectTo: '/'                    // "/"に飛ぶ
        });
    }]);
})(window.app);