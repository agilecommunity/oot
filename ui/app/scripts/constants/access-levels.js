define(['constants/user_roles'],
function (UserRoles) {
    "use strict";

    var AccessLevels = {  // ページのアクセスレベル
        public: UserRoles.public | // 111
            UserRoles.user |
            UserRoles.admin,
        anon: UserRoles.public,  // 001
        user: UserRoles.user |   // 110
            UserRoles.admin,
        admin: UserRoles.admin    // 100
    };

    return AccessLevels;
});