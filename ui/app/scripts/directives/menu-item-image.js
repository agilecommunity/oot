(function() {

    angular.module('MyDirectives')
        .directive('ootMenuItemImage', ootMenuItemImage);

    ootMenuItemImage.$inject = ["$compile", "$filter", "Assets"];

    function ootMenuItemImage($compile, $filter, Assets) {

        var noImageFile = "no-image.png";

        var getImagePath = function(menuItem) {
            var imgFile = noImageFile;
            if ($filter("isEmptyOrUndefined")(menuItem.itemImagePath) !== true ) {
                imgFile = menuItem.itemImagePath;
            }

            return Assets.versioned("/uc-assets/images/menu-items/" + imgFile);
        };

        var getFullName = function(menuItem) {
            return menuItem.shopName + " " + menuItem.itemNumber + " " + menuItem.name;
        };

        var linker = function(scope, element, attrs) {

            var doCompile = function() {
                element.attr("src", getImagePath(scope.menuItem));
                element.attr("alt", getFullName(scope.menuItem));
            };

            scope.$watch('menuItem', doCompile);
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