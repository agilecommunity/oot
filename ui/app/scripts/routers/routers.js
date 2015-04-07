(function (app) {
    "use strict";

    var UserRoles = app.UserRoles;
    var AccessLevels = app.AccessLevels;

    return app.config(
        ['$routeProvider', '$httpProvider',    // ルーティングの定義
        function ($routeProvider, $httpProvider) {

        $routeProvider
        .when('/', {                         // AngularJS上でのパス
            templateUrl: '/views/signin',    // 利用するビュー
            controller: 'SigninController',  // 利用するコントローラー
            controllerAs: 'vm',
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
        .when('/reset', {
            templateUrl: '/views/reset-password/start-reset',
            controller: 'StartResetPasswordController',
            controllerAs: 'vm',
            access: AccessLevels.anon
        })
        .when('/reset/:token', {
            templateUrl: '/views/reset-password/reset',
            controller: 'ResetPasswordController',
            controllerAs: 'vm',
            access: AccessLevels.anon
        })
        .when('/order', {
            templateUrl: '/views/order',
            controller: 'OrderController',
            controllerAs: 'vm',
            access: AccessLevels.user,
            resolve: app.my.resolvers.OrderController
        })
        .when('/view-order/:menuDate?', {
            templateUrl: '/views/view-order',
            controller: 'ViewOrderController',
            controllerAs: 'vm',
            resolve: app.my.resolvers.ViewOrderController,
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
        .when('/admin/checklist/:menuDate', {
            templateUrl: '/views/admin/checklist/daily',
            controller: 'AdminChecklistDailyController',
            reloadOnSearch: false,
            resolve: app.my.resolvers.AdminChecklistDailyController,
            access: AccessLevels.admin
        })
        .when('/admin/checklist/weekly/:startDate', {
            templateUrl: '/views/admin/checklist/weekly',
            controller: 'AdminChecklistWeeklyController',
            reloadOnSearch: false,
            resolve: app.my.resolvers.AdminChecklistWeeklyController,
            access: AccessLevels.admin
        })
        .when('/admin/purchase-order/:orderDate/confirmation', {
            templateUrl: '/views/admin/purchase-order-confirmation',
            controller: 'AdminPurchaseOrderConfirmationController',
            reloadOnSearch: false,
            resolve: app.my.resolvers.AdminPurchaseOrderConfirmationController,
            access: AccessLevels.admin
        })
        .when('/admin/purchase-order/:orderDate', {
            templateUrl: '/views/admin/purchase-order',
            controller: 'AdminPurchaseOrderController',
            reloadOnSearch: false,
            resolve: app.my.resolvers.AdminPurchaseOrderController,
            access: AccessLevels.admin
        })
        .when('/admin/cash-book/:targetDate', {
            templateUrl: '/views/admin/cash-book',
            controller: 'AdminCashBookController',
            reloadOnSearch: false,
            resolve: app.my.resolvers.AdminCashBookController,
            access: AccessLevels.admin
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
        .when('/admin/users/index', {
            templateUrl: '/views/admin/user/index',
            controller: 'UserIndexController',
            resolve: app.my.resolvers.UserIndexController,
            access: AccessLevels.admin
        })
        .when('/admin/settings', {
            templateUrl: '/views/admin/settings',
            controller: 'AdminSettingsController',
            controllerAs: 'vm',
            resolve: app.my.resolvers.AdminSettingsController,
            access: AccessLevels.admin
        })
        .otherwise({                           // その他のパスが指定された場合
            redirectTo: '/'                    // "/"に飛ぶ
        });
    }]);
})(window.app);