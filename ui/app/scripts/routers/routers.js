(function (app) {
    "use strict";

    var UserRoles = app.UserRoles;
    var AccessLevels = app.AccessLevels;

    return app.config(
        ['$routeProvider', '$httpProvider', 'appConfig', // ルーティングの定義
        function ($routeProvider, $httpProvider, appConfig) {

        var versionedTemplateUrl = function(url) {
            if (appConfig.appMode === 'prod') {
                return appConfig.versionedViews[url];
            } else {
                return url;
            }
        };

        $routeProvider
        .when('/', {                                               // AngularJS上でのパス
            templateUrl: versionedTemplateUrl('/views/signin'),    // 利用するビュー
            controller: 'SigninController',                        // 利用するコントローラー
            controllerAs: 'vm',                                    // ビュー上でのコントローラのエイリアス
            resolve: app.my.resolvers.SigninController,            // データの事前読み込み
            access: AccessLevels.anon                              // アクセス権 (アプリで追加した項目)
        })
        .when('/signup', {
            templateUrl: versionedTemplateUrl('/views/start-signup'),
            controller: 'StartSignupController',
            access: AccessLevels.anon
        })
        .when('/signup/:token', {
            templateUrl: versionedTemplateUrl('/views/signup'),
            controller: 'SignupController',
            access: AccessLevels.anon
        })
        .when('/reset', {
            templateUrl: versionedTemplateUrl('/views/reset-password/start-reset'),
            controller: 'StartResetPasswordController',
            controllerAs: 'vm',
            access: AccessLevels.anon
        })
        .when('/reset/:token', {
            templateUrl: versionedTemplateUrl('/views/reset-password/reset'),
            controller: 'ResetPasswordController',
            controllerAs: 'vm',
            access: AccessLevels.anon
        })
        .when('/order', {
            templateUrl: versionedTemplateUrl('/views/order'),
            controller: 'OrderController',
            controllerAs: 'vm',
            resolve: app.my.resolvers.OrderController,
            access: AccessLevels.user
        })
        .when('/view-order/:menuDate?', {
            templateUrl: versionedTemplateUrl('/views/view-order'),
            controller: 'ViewOrderController',
            controllerAs: 'vm',
            resolve: app.my.resolvers.ViewOrderController,
            access: AccessLevels.user
        })
        .when('/admin/index', {
            templateUrl: versionedTemplateUrl('/views/admin/index'),
            controller: 'AdminIndexController',
            controllerAs: 'vm',
            resolve: app.my.resolvers.AdminIndexController,
            access: AccessLevels.admin
        })
        .when('/admin/daily-menus/:menuDate?', {
            templateUrl: versionedTemplateUrl('/views/admin/daily-menu/edit'),
            controller: 'DailyMenuEditController',
            controllerAs: 'vm',
            access: AccessLevels.admin
        })
        .when('/admin/checklist/:menuDate', {
            templateUrl: versionedTemplateUrl('/views/admin/checklist/daily'),
            controller: 'AdminChecklistDailyController',
            controllerAs: 'vm',
            reloadOnSearch: false,
            resolve: app.my.resolvers.AdminChecklistDailyController,
            access: AccessLevels.admin
        })
        .when('/admin/checklist/weekly/:startDate', {
            templateUrl: versionedTemplateUrl('/views/admin/checklist/weekly'),
            controller: 'AdminChecklistWeeklyController',
            controllerAs: 'vm',
            reloadOnSearch: false,
            resolve: app.my.resolvers.AdminChecklistWeeklyController,
            access: AccessLevels.admin
        })
        .when('/admin/purchase-order/:orderDate/confirmation', {
            templateUrl: versionedTemplateUrl('/views/admin/purchase-order-confirmation'),
            controller: 'AdminPurchaseOrderConfirmationController',
            reloadOnSearch: false,
            resolve: app.my.resolvers.AdminPurchaseOrderConfirmationController,
            access: AccessLevels.admin
        })
        .when('/admin/purchase-order/:orderDate', {
            templateUrl: versionedTemplateUrl('/views/admin/purchase-order'),
            controller: 'AdminPurchaseOrderController',
            reloadOnSearch: false,
            resolve: app.my.resolvers.AdminPurchaseOrderController,
            access: AccessLevels.admin
        })
        .when('/admin/cash-book/:targetDate', {
            templateUrl: versionedTemplateUrl('/views/admin/cash-book'),
            controller: 'AdminCashBookController',
            reloadOnSearch: false,
            resolve: app.my.resolvers.AdminCashBookController,
            access: AccessLevels.admin
        })
        .when('/admin/menu-items/index', {
            templateUrl: versionedTemplateUrl('/views/admin/menu-item/index'),
            controller: 'MenuItemIndexController',
            access: AccessLevels.admin
        })
        .when('/admin/menu-items/import', {
            templateUrl: versionedTemplateUrl('/views/admin/menu-item/import'),
            controller: 'MenuItemImportController',
            access: AccessLevels.admin
        })
        .when('/admin/menu-item-images/import', {
            templateUrl: versionedTemplateUrl('/views/admin/menu-item-image/import'),
            controller: 'MenuItemImageImportController',
            access: AccessLevels.admin
        })
        .when('/admin/users/index', {
            templateUrl: versionedTemplateUrl('/views/admin/user/index'),
            controller: 'UserIndexController',
            resolve: app.my.resolvers.UserIndexController,
            access: AccessLevels.admin
        })
        .when('/admin/settings', {
            templateUrl: versionedTemplateUrl('/views/admin/settings'),
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