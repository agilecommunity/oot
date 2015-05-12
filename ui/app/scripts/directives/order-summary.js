
angular.module('MyDirectives')
.directive('ootOrderSummary', function ($compile, $filter) {

    function templateForImage(detailItem) {
        var template = "";

        if ($filter("isEmptyOrUndefined")(detailItem.menuItem.itemImagePath) !== true ) {
            template = "<img class='order-first-item-image' src='/uc-assets/images/menu-items/" + detailItem.menuItem.itemImagePath + "' width=80px>";
        }

        return template;
    }

    function templateForReducedOnOrder(order) {
        var template = "<p class='order-item-reduced-on-order'>" + $filter('currencyNoFraction')(order.totalReducedOnOrder(), "") + "円" + "</p>";
        return template;
    }

    var linker = function(scope, element, attrs) {
        scope.$watch('order', function(newValue, oldValue){

            var template = "(なし)";

            if (newValue !== undefined && newValue !== null && newValue.detailItems.length > 0) {

                template = templateForImage(newValue.detailItems[0]) +
                    "<p class='order-first-item'>{{order.detailItems[0].menuItem.name}} {{order.detailItems[0].numOrders}}個</p>";

                if (newValue.detailItems.length > 1) {
                    template += "<div class='order-other-items'>他{{order.detailItems.length - 1}}件</div>";
                }

                template += templateForReducedOnOrder(newValue);
            }

            element.html(template).show();

            $compile(element.contents())(scope);
        });
    };

    return {
        restrict: "A",
        link: linker,
        scope: {
            order: "="
        }
    };
});
