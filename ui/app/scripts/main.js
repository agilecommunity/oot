(function ($,
          angular,
          app) {
    "use strict";

    var UserRoles = app.UserRoles;
    var AccessLevels = app.AccessLevels;

    $(function () {

        app.run(["$rootScope", "$location", "$window", "User", "RouteFinder",
                 function ($rootScope, $location, $window, User, RouteFinder) {

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
                        alert("ページにアクセスできる権限がありません。");
                        ev.preventDefault();
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
                var message = "画面の表示中にエラーが発生しました。元の画面に戻ります。";

                if (rejection) {
                    message += "<br/>" + "ステータス:" + rejection.status;
                    message += "&nbsp;";
                    message += "原因:" + rejection.reason;
                }

                bootbox.alert(message, function(){
                    // $location.path だと上手く遷移してくれなかったので…
                    $window.history.back();
                });
            });
        }]);

        angular.bootstrap(document, ['oot']);
    });
})(jQuery, angular, window.app);
