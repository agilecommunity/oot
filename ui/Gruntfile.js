'use strict';

// https://github.com/leon/play-grunt-angular-prototype/blob/master/ui/Gruntfile.js を参考に
module.exports = function (grunt) {

    grunt.loadNpmTasks('grunt-contrib-concat');
    grunt.loadNpmTasks('grunt-contrib-copy');

    // configurable paths
    var appConfig = {
        components: 'app/bower_components',
        javascripts: 'app/scripts',
        stylesheets: 'app/styles',
        dist: {
            javascripts: '../public/javascripts',
            stylesheets: '../public/stylesheets'
        }
    };

    grunt.initConfig({
        conf: appConfig,
        concat: {
            options: {
                separator: ';'
            }, angular: {
                dest: '<%= conf.dist.javascripts %>/angular.min.js', src: [
                    '<%= conf.components %>/angular/angular.min.js'
                    , '<%= conf.components %>/angular-resource/angular-resource.min.js'
                    , '<%= conf.components %>/angular-route/angular-route.min.js'
                ]
            }, ieshim: {
                dest: '<%= conf.dist.javascripts %>/ie-shim.min.js', src: [
                    '<%= conf.components %>/json2/json2.js'
                    , '<%= conf.components %>/es5-shim/es5-shim.min.js'
                ]
            }
        }, copy: {
            javascripts: {
                dest: '<%= conf.dist.javascripts %>/'
                , flatten: true
                , src: [
                    '<%= conf.components %>/bootstrap/dist/js/bootstrap.min.js'
                    , '<%= conf.components %>/jquery/jquery.min.js'
                    , '<%= conf.components %>/moment/min/moment-with-langs.min.js'
                    , '<%= conf.javascripts %>/app.js'
                ], expand: true
            }, stylesheets: {
                dest: '<%= conf.dist.stylesheets %>/'
                , flatten: true
                , src: [
                    '<%= conf.components %>/bootstrap/dist/css/bootstrap.min.css'
                    , '<%= conf.stylesheets %>/common.css'
                ], expand: true
            }
        }
    });

    grunt.registerTask('dev', [
        'concat'
        , 'copy'
    ]);

    grunt.registerTask('default', ['dev']);
}