define(['jquery',
        'angular',
        'moment',
        'angular.resource',
        'angular.route',
        'angular.sanitize',
        'underscore',
        'services/services',
        'controllers/controllers',
        'filters/filters'],
function ($,
          angular,
          moment) {
    "use strict";

    moment.lang('ja');

    var app = angular.module('oot',
            [  // アプリケーションの定義
               'ngRoute',            // 依存するサービスを指定する
               'ngResource',
               'ngSanitize',
               'MyServices',
               'MyControllers',
               'MyFilters'
            ]
    );

    return app;

});
