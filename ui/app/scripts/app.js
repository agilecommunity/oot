define(['jquery',
        'angular',
        'moment',
        'angular.resource',
        'angular.route',
        'services/services',
        'controllers/controllers',
        'filters/filters'],
function ($,
          angular,
          moment) {
    "use strict";

    var app = angular.module('oot',
            [  // アプリケーションの定義
               'ngRoute',            // 依存するサービスを指定する
               'ngResource',
               'MyServices',
               'MyControllers',
               'MyFilters'
            ]
    );

    return app;

});
