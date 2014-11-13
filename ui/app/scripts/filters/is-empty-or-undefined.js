
angular.module('MyFilters')
    .filter('isEmptyOrUndefined', function () {
        return function (input) {
            return (input === undefined || input === null || input.length === 0);
        };
    });

