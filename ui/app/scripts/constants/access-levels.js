(function (app) {
    "use strict";

    var UserRoles = app.UserRoles;

    app.AccessLevels = {  // ページのアクセスレベル
        public: UserRoles.public | // 111
            UserRoles.user |
            UserRoles.admin,
        anon: UserRoles.public,  // 001
        user: UserRoles.user |   // 110
            UserRoles.admin,
        admin: UserRoles.admin    // 100
    };

})(window.app);