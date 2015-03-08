(function ($,
          angular,
          app) {
    "use strict";

    var UserRoles = app.UserRoles;
    var AccessLevels = app.AccessLevels;

    $(function () {

        app.config(['dialogsProvider',
            function(dialogsProvider) {
            dialogsProvider.useBackdrop('static');
            dialogsProvider.useEscClose(false);
            dialogsProvider.useCopy(false);
        }]);

        app.run(["$rootScope", "$location", "$window", "User", "RouteFinder", "dialogs",
                 function ($rootScope, $location, $window, User, RouteFinder, dialogs) {

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
                        dialogs.error("エラー", "ページにアクセスできる権限がありません。");
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
                    dialog = dialogs.error("認証エラー", "サインインがまだか、セッションがタイムアウトしました。<br>サインイン画面に戻ります。");
                    urlToGo = "/";
                } else {
                    var messages = ["画面の表示中にエラーが発生しました。元の画面に戻ります。"];

                    if (rejection) {
                        messages.push("ステータス:" + rejection.status);
                        messages.push("URL:" + rejection.config.url);

                        if (rejection.reason !== undefined) {
                            messages.push("原因:" + rejection.reason.message);
                        }
                    }

                    if (previous === undefined) {
                        urlToGo = "/";
                    } else {
                        urlToGo = previous.originalPath;
                    }

                    dialog = dialogs.error("エラー", messages.join("<br>"));
                }

                dialog.result["finally"](function(){
                    $location.path(urlToGo);
                });
            });
        }]);

        angular.bootstrap(document, ['oot']);
    });
})(jQuery, angular, window.app);
