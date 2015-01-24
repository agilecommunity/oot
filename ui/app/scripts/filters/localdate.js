angular.module('MyFilters')
.filter('localdate', function() {
    return function(input, formatStr) {
        return moment.utc(input).format(formatStr);
    };
});
