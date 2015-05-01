/* http://blog.tompawlak.org/remove-decimal-cents-angularjs-currency-formatting より*/
(function(){
    angular.module('MyFilters')
        .filter('currencyNoFraction', currencyNoFraction);

    currencyNoFraction.$inject = ['$filter', '$locale'];

    function currencyNoFraction($filter, $locale) {

        var currency = $filter('currency'), formats = $locale.NUMBER_FORMATS;

        return function (amount, symbol) {
            var value = currency(amount, symbol);
            return value.replace(
                new RegExp('\\' + formats.DECIMAL_SEP + '\\d{2}'), '');
        };
    }
})();
