(function ($,
          angular,
          app) {
    "use strict";

    var UserRoles = app.UserRoles;
    var AccessLevels = app.AccessLevels;

    $(function () {

        app.config(appConfiguration);

        appConfiguration.$inject = ['dialogsProvider'];

        function appConfiguration(dialogsProvider) {
            dialogsProvider.useBackdrop('static');
            dialogsProvider.useEscClose(false);
            dialogsProvider.useCopy(false);
        }

        app.run(appRun);

        appRun.$inject = ["$rootScope", "$location", "$window", "User", "RouteFinder", "MyDialogs", "Assets"];

        function appRun($rootScope, $location, $window, User, RouteFinder, MyDialogs, Assets) {
            // ViewでAssetsを呼び出せるようにする
            $rootScope.Assets = Assets;

            // ブラウザのリロード対策
            $rootScope.$on('$locationChangeStart', function (ev, next, current) {

                // ルートの情報を取得
                var route = RouteFinder.parseRoute().$$route;

                if (route.access === AccessLevels.anon) {
                    return;
                }

                // すでに認証済みの場合
                if (User.isSignedIn()) {

                    // アクセスチェック
                    if (User.isAccessible(route.access, User.currentUser()) === true) {
                        return;
                    } else {
                        ev.preventDefault();
                        MyDialogs.error("エラー", "ページにアクセスできる権限がありません。");
                        return;
                    }
                }

                // 現在持っているトークンを使って再認証する
                User.reSignin({
                    success: function () {
                        // 何もしない
                    },
                    error: function () {
                        ev.preventDefault();
                        $location.path("/");
                    }
                });
            });

            $rootScope.$on('$routeChangeError', function(event, current, previous, rejection){

                console.log("$routeChangeError");
                console.log(event);
                console.log(current);
                console.log(previous);
                console.log(rejection);

                var dialog = "";
                var urlToGo = "";

                if (rejection && rejection.status === 401) {
                    dialog = MyDialogs.error("認証エラー", "サインインがまだか、セッションがタイムアウトしました。<br>サインイン画面に戻ります。");
                    dialog.result["finally"](function(){
                        $location.path("/");
                    });
                } else {
                    dialog = MyDialogs.serverError("サーバーエラー", rejection);
                    // 何しても無駄かもしれないので、パスの移動はしないことにする
                }
            });
        }

        angular.bootstrap(document, ['oot']);
    });
})(jQuery, angular, window.app);
