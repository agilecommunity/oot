
angular.module('MyDirectives')
.directive('ootReducedPrice', function ($compile) {
    var defaultTemplate = "{{menuItem.reducedOnOrder}}円";
    var discountTemplate = "{{menuItem.fixedOnOrder}}円 - {{menuItem.discountOnOrder}}円＝{{menuItem.reducedOnOrder}}円";

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
});
