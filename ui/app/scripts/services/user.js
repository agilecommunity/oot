var AccessLevels = app.AccessLevels;

angular.module('MyServices')
.factory('User',
    ['$http', '$rootScope', '$resource',
    function ($http, $rootScope, $resource) {  // ユーザ認証を行うサービス

    $rootScope.currentUser = null;

    var User = $resource('/api/v1.0/users/:id', { id: "@id" });

    User.currentUser = function () {
        return $rootScope.currentUser;
    };

    User.isAccessible = function (access, user) {
        // 誰でもアクセス可能な場合は、true
        if (access === AccessLevels.anon || access === AccessLevels.public) {
            return true;
        }

        // サインインが必要な場合は、userオブジェクトがnullでなければOK
        if (access === AccessLevels.user && user !== null) {
            return true;
        }

        if (access === AccessLevels.admin && user !== null && user.isAdmin === true) {
            return true;
        }

        return false;
    };

    User.isSignedIn = function () {
        return ($rootScope.currentUser !== null);
    };

    // ユーザ名、パスワードで認証を行う
    User.signin = function (username, password, callback) {
        var parameter = {};
        parameter.username = username;
        parameter.password = password;

        $http({
            method: 'POST',
            url: '/api/v1.0/signin/userpass',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' },
            transformRequest: function(data) {
                return jQuery.param(data);
            },
            data: parameter
        })
        .success(function (data, status, header) {
            $rootScope.currentUser = data;
            callback.success();
        })
        .error(function (data, status, header) {
            $rootScope.currentUser = null;
            callback.error({data: data, status: status});
        });
    };

    // 取得しているトークンで認証情報を所得してみる
    User.reSignin = function (callback) {
        $http.get("/api/v1.0/users/me")
        .success(function (data, status, header) {
            $rootScope.currentUser = data;
            callback.success();
        })
        .error(function (data, status, header) {
            $rootScope.currentUser = null;
            callback.error();
        });
    };

    User.signout = function(callback) {
        if ($rootScope.currentUser === null) {
            return;
        }

        $http({
            method: 'GET',
            url: '/api/v1.0/signout'
        })
        .success(function (data, status, header) {
            $rootScope.currentUser = null;
            callback.success();
        })
        .error(function (data, status, header) {
            $rootScope.currentUser = null;
            callback.error({data: data, status: status});
        });
    };

    return User;

}]);
