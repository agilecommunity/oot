'use strict';

// https://github.com/leon/play-grunt-angular-prototype/blob/master/ui/Gruntfile.js を参考に
module.exports = function (grunt) {

    grunt.loadNpmTasks('grunt-contrib-concat');
    grunt.loadNpmTasks('grunt-contrib-copy');

    // configurable paths
    var appConfig = {
        components: 'app/bower_components',
        javascripts: 'app/scripts',
        stylesheets: 'app/stylesheets',
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
                dest: '<%= conf.dist.javascripts %>/angular.min.js'
                , src: [
                    '<%= conf.components %>/angular/angular.min.js'
                    , '<%= conf.components %>/angular-resource/angular-resource.min.js'
                    , '<%= conf.components %>/angular-route/angular-route.min.js'
                ]
            }
        }
    });

    grunt.registerTask('dev', [
        'concat'
    ]);

    grunt.registerTask('default', ['dev']);
}