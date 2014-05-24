
angular.module('MyServices')
.factory('User',
    ['$http', '$rootScope',
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
}]);
