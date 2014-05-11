define(['app',
        'constants/user-roles',
        'constants/access-levels',
        'controllers/controllers'],
function (app,
          UserRoles,
          AccessLevels) {
    "use strict";

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
});