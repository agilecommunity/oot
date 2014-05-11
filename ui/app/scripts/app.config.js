require.config({
    baseUrl: "/assets/javascripts",
    paths: {
        jquery : 'components/jquery.min',
        angular : 'components/angular.min',
        "angular.resource" : 'components/angular-resource.min',
        "angular.route" : 'components/angular-route.min',
        bootstrap : 'components/bootstrap.min',
        'moment' : 'components/moment-with-langs.min',
        'datetimepicker' : 'components/bootstrap-datetimepicker.min'
    },

    shim: {
        jquery: {
            exports: "jQuery"
        },
        angular: {
            exports: "angular"
        },
        "angular.resource": {
            deps: ["angular"],
            exports: "angular"
        },
        "angular.route": {
            deps: ["angular"],
            exports: "angular"
        },
        bootstrap: {
            deps: ["jquery"],
            exports: "$.fn.popover"
        },
        "moment" : {
            exports: "moment"
        },
        "datetimepicker" : {
            deps: ["jquery", "moment", "bootstrap"],
            exports: "$.fn.datetimepicker"
        }
    },
    enforceDefine : true // IE8対策
});