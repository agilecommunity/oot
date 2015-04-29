(function() {

    angular.module('MyServices')
        .factory('Assets', Assets);

    Assets.$inject = ['appConfig'];

    function Assets(appConfig) {
        var MyClass = {};

        MyClass.versioned = function(path) {
            var versionedPath = appConfig.versionedViews[path];
            if (appConfig.appMode === 'dev' || versionedPath === undefined) {
                versionedPath = path;
            }
            return versionedPath;
        };

        return MyClass;
    }

})();
