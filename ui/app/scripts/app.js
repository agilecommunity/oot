(function(window) {
    "use strict";

    moment.locale('ja');
    moment.tz.add("Asia/Tokyo|JCST JST JDT|-90 -90 -a0|0121212121|-1iw90 pKq0 QL0 1lB0 13X0 1zB0 NX0 1zB0 NX0");
    moment.tz.setDefault('Asia/Tokyo');

    angular.module('MyServices', ['ngResource', 'ngRoute']);
    angular.module('MyControllers', []);
    angular.module('MyFilters', []);
    angular.module('MyDirectives', []);

    angular.module('underscore.string', []).factory("_s", function(){ return s;});

    window.app = angular.module('oot',
        [  // アプリケーションの定義
            'ngRoute',            // 依存するサービスを指定する
            'ngResource',
            'ngSanitize',
            'ui.bootstrap',
            'angularSpinner',
            'underscore.string',
            'MyServices',
            'MyControllers',
            'MyFilters',
            'MyDirectives'
        ]
    );

    window.app.my = {};
    window.app.my.resolvers = {};
    window.app.my.helpers = {};

})(window);
