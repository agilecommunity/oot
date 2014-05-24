
angular.module('MyFilters')
.filter('checkmark', function () {
    return function (input) {
        return input ? '○' : '';
    };
});

