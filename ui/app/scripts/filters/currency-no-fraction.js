/* http://blog.tompawlak.org/remove-decimal-cents-angularjs-currency-formatting より*/
angular.module('MyFilters')
.filter('currencyNoFraction', [ '$filter', '$locale', function ($filter, $locale) {
    var currency = $filter('currency'), formats = $locale.NUMBER_FORMATS;
    return function (amount, symbol) {
        var value = currency(amount, symbol);
        return value.replace(
            new RegExp('\\' + formats.DECIMAL_SEP + '\\d{2}'), '');
    };
}]);
