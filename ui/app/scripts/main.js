(function ($,
          angular,
          app) {
    "use strict";

    var UserRoles = app.UserRoles;
    var AccessLevels = app.AccessLevels;

    $(function () {

        app.run(["$rootScope", "$location", "User", "RouteFinder",
                 function ($rootScope, $location, User, RouteFinder) {

            // ブラウザのリロード対策
            $rootScope.$on('$locationChangeStart', function (ev, next, current) {

                // ルートの情報を取得
                var route = RouteFinder.parseRoute().$$route;

                if (route.access === AccessLevels.anon) {
                    return;
                }

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
})(jQuery, angular, window.app);
