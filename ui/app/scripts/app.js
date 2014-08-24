(function(window) {
    "use strict";

    moment.lang('ja');

    angular.module('MyServices', ['ngResource', 'ngRoute']);
    angular.module('MyControllers', []);
    angular.module('MyFilters', []);

    window.app = angular.module('oot',
        [  // アプリケーションの定義
            'ngRoute',            // 依存するサービスを指定する
            'ngResource',
            'ngSanitize',
            'MyServices',
            'MyControllers',
            'MyFilters'
        ]
    );

})(window);
