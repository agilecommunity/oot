// https://github.com/leon/play-grunt-angular-prototype/blob/master/ui/Gruntfile.js を参考に
module.exports = function (grunt) {
    "use strict";

    grunt.loadNpmTasks('grunt-contrib-concat');
    grunt.loadNpmTasks('grunt-contrib-copy');
    grunt.loadNpmTasks('grunt-contrib-watch');
    grunt.loadNpmTasks('grunt-jsdoc');
    grunt.loadNpmTasks('grunt-contrib-jshint');
    grunt.loadNpmTasks('grunt-notify');

    // configurable paths
    var appConfig = {
        components: 'app/bower_components',
        javascripts: 'app/scripts',
        stylesheets: 'app/styles',
        dist: {
            components:  '../public/javascripts/components',
            javascripts: '../public/javascripts',
            stylesheets: '../public/stylesheets',
            fonts:        '../public/fonts',
            docs:         '../target/docs/ui'
        }
    };

    grunt.initConfig({
        conf: appConfig,
        watch: {
            scripts: {
                files: [
                    '<%= conf.javascripts %>/**',
                    '<%= conf.stylesheets %>/*'
                ],
                tasks: ['jshint', 'concat', 'copy'],
                options: {
                    spawn: false
                }
            }
        },
        concat: {
            options: {
                separator: ';'
            }, ieshim: {
                dest: '<%= conf.dist.components %>/ie-shim.min.js',
                src: [
                    '<%= conf.components %>/json2/json2.js',
                    '<%= conf.components %>/es5-shim/es5-shim.min.js'
                ]
            },
            constants: {
                dest: '<%= conf.dist.javascripts %>/constants/constants.js',
                src: [
                    '<%= conf.javascripts %>/constants/user-roles.js',
                    '<%= conf.javascripts %>/constants/access-levels.js'
                ]
            },
            filters: {
                dest: '<%= conf.dist.javascripts %>/filters/filters.js',
                src: [
                    '<%= conf.javascripts %>/filters/[a-z]*.js'
                ]
            },
            controllers: {
                dest: '<%= conf.dist.javascripts %>/controllers/controllers.js',
                src: [
                    '<%= conf.javascripts %>/controllers/**/[a-z]*.js'
                ]
            },
            services: {
                dest: '<%= conf.dist.javascripts %>/services/services.js',
                src: [
                    '<%= conf.javascripts %>/services/_helpers.js',
                    '<%= conf.javascripts %>/services/**/[a-z]*.js'
                ]
            }
        },
        copy: {
            components: {
                dest: '<%= conf.dist.components %>/',
                flatten: true,
                src: [
                    '<%= conf.components %>/requirejs/require.js',
                    '<%= conf.components %>/jquery/jquery.min.js',
                    '<%= conf.components %>/angular/angular.min.js',
                    '<%= conf.components %>/angular-resource/angular-resource.min.js',
                    '<%= conf.components %>/angular-route/angular-route.min.js',
                    '<%= conf.components %>/angular-sanitize/angular-sanitize.min.js',
                    '<%= conf.components %>/bootstrap/dist/js/bootstrap.min.js',
                    '<%= conf.components %>/bootstrap/js/collapse.js',
                    '<%= conf.components %>/bootstrap/js/transition.js',
                    '<%= conf.components %>/angular-bootstrap/ui-bootstrap.js',
                    '<%= conf.components %>/angular-bootstrap/ui-bootstrap-tpls.js',
                    '<%= conf.components %>/spin.js/spin.js',
                    '<%= conf.components %>/angular-spinner/angular-spinner.js',
                    '<%= conf.components %>/moment/min/moment-with-locales.js',
                    '<%= conf.components %>/moment-timezone/moment-timezone.js',
                    '<%= conf.components %>/moment-timezone/moment-timezone-utils.js',
                    '<%= conf.components %>/eonasdan-bootstrap-datetimepicker/src/js/bootstrap-datetimepicker.js',
                    '<%= conf.components %>/holderjs/holder.js',
                    '<%= conf.components %>/html5shiv/dist/html5shiv.js',
                    '<%= conf.components %>/respond/dest/respond.min.js',
                    '<%= conf.components %>/underscore/underscore.js',
                    '<%= conf.components %>/bootbox/bootbox.js',
                    '<%= conf.components %>/blockui/jquery.blockUI.js',
                    '<%= conf.components %>/jquery-file-upload/js/vendor/jquery.ui.widget.js',
                    '<%= conf.components %>/jquery-file-upload/js/jquery.iframe-transport.js',
                    '<%= conf.components %>/jquery-file-upload/js/jquery.fileupload.js',
                    '<%= conf.components %>/jquery-file-upload/js/jquery.fileupload-angular.js'
                ],
                expand: true
            },
            javascripts: {
                dest: '<%= conf.dist.javascripts %>/',
                flatten: true,
                src: [
                    '<%= conf.javascripts %>/*.js'
                ], expand: true
            },
            javascriptsSubDirectories: {
                dest: '<%= conf.dist.javascripts %>',
                cwd: '<%= conf.javascripts %>/',
                src: [
                    'constants/*.js',
                    'helpers/*.js',
                    'routers/*.js'
                ],
                expand: true
            },
            stylesheets: {
                dest: '<%= conf.dist.stylesheets %>/',
                flatten: true,
                src: [
                    '<%= conf.components %>/bootstrap/dist/css/bootstrap.min.css',
                    '<%= conf.components %>/eonasdan-bootstrap-datetimepicker/build/css/bootstrap-datetimepicker.min.css',
                    '<%= conf.components %>/jquery-file-upload/css/jquery.fileupload.css',
                    '<%= conf.components %>/jquery-file-upload/css/jquery.fileupload-ui.css',
                    '<%= conf.stylesheets %>/common.css'
                ],
                expand: true
            }, fonts: {
                dest: '<%= conf.dist.fonts %>/',
                flatten: true,
                src: [
                    '<%= conf.components %>/bootstrap/fonts/*'
                ],
                expand: true
            }
        },
        jsdoc: {
            dist: {
                src: ['<%= conf.javascripts %>/*.js'], // JSDoc化したいソースコードへのパス
                options: {
                    destination: '<%= conf.dist.docs %>', // 出力先パス
                    configure: 'jsdoc-config.json' // docstrapの設定ファイル
                }
            }
        },
        jshint: {
            all: [
                'Gruntfile.js',
                '<%= conf.javascripts %>/**/*.js',
                '!<%= conf.javascripts %>/**/_intro.js',
                '!<%= conf.javascripts %>/**/_outro.js'
            ]
        },
        notify: {

        }
    });

    grunt.registerTask('dev', [
       'jshint',
       'concat',
       'copy'
    ]);

    grunt.registerTask('default', ['dev', 'watch', 'notify']);
};