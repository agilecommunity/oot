(function(){

    angular.module('MyFilters')
        .filter('null2nbsp', null2nbsp);

    null2nbsp.$inject = ['$filter'];

    function null2nbsp($filter) {
        return function(input) {
            return ($filter('isEmptyOrUndefined')(input) === true) ? '\u00A0' : input;
        };
    }
})();