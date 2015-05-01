(function(){

    angular.module('MyDirectives')
        .directive('ootReducedPrice', ootReducedPrice);

    ootReducedPrice.$inject = ["$compile"];

    function ootReducedPrice($compile) {

        var defaultTemplate = "{{menuItem.reducedOnOrder | currencyNoFraction:\"\"}}円";
        var discountTemplate = "{{menuItem.fixedOnOrder | currencyNoFraction:\"\"}}円 - {{menuItem.discountOnOrder | currencyNoFraction:\"\"}}円＝{{menuItem.reducedOnOrder | currencyNoFraction:\"\"}}円";

        var linker = function(scope, element, attrs) {
            scope.$watch('menuItem', function(){
                var template = defaultTemplate;
                if (scope.menuItem.discountOnOrder > 0) {
                    template = discountTemplate;
                }

                element.html(template).show();

                $compile(element.contents())(scope);
            });
        };

        return {
            restrict: "A",
            link: linker,
            scope: {
                menuItem: "="
            }
        };
    }

})();
