(function(){

    angular.module('MyDirectives')
        .directive('ootGatheringStatus', gatheringStatus);

    function gatheringStatus($compile) {

        var defaultTemplate =
           "<span class=\"glyphicon glyphicon-exclamation-sign\" aria-hidden=\"true\"></span> " +
           "<span class=\"gathering-status-remain\">あと{{gatheringSetting.minOrders - orderStat.bentoStat.numOrders}}個</span>で<br>" +
           "<span class=\"gathering-status-discount\">{{gatheringSetting.discountPrice}}円引き!!!<br></span>" +
           "<span class=\"gathering-status-min-orders\">目標 {{gatheringSetting.minOrders}}個</span>";
        var achievedTemplate =
           "<span class=\"glyphicon glyphicon-ok-sign\" aria-hidden=\"true\"></span> " +
           "<span class=\"gathering-status-achieved\">{{gatheringSetting.discountPrice}}円引き達成!!!</span><br>" +
           "<span class=\"gathering-status-current-orders\">現在 {{orderStat.bentoStat.numOrders}}個</span>";

        var linker = function(scope, element, attrs) {

            var doCompile = function() {
                var template = defaultTemplate;
                if (scope.gatheringSetting.isAchieved(scope.orderStat.bentoStat.numOrders)) {
                    template = achievedTemplate;
                }

                if (scope.gatheringSetting.enabled) {
                    element.removeClass("hidden");
                    element.html(template).show();
                } else {
                    element.addClass("hidden");
                }

                $compile(element.contents())(scope);
            };

            scope.$watch('orderStat', doCompile);
            scope.$watch('gatheringSetting', doCompile);
        };

        return {
            restrict: "A",
            link: linker,
            scope: {
                gatheringSetting: "=",
                orderStat: "="
            }
        };
    }

})();
