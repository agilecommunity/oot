
angular.module('MyFilters')
.filter('karamoji', function () {
    return function (input) {
        return (!angular.isString(input) || input === "") ? '(空)' : input;
    };
});

