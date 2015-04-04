(function(){

    angular.module('MyDirectives')
        .directive('ootGatheringStatus', gatheringStatus);

    function gatheringStatus($compile) {

        var defaultTemplate =
           "<span class=\"glyphicon glyphicon-exclamation-sign\" aria-hidden=\"true\"></span> " +
           "<span class=\"gathering-status-remain\">あと{{gatheringSettings.minOrders - orderStat.numOrders}}個</span>で<br>" +
           "<span class=\"gathering-status-discount\">{{gatheringSettings.discountPrice}}円引き!!!<br></span>" +
           "<span class=\"gathering-status-min-orders\">目標 {{gatheringSettings.minOrders}}個</span>";
        var achievedTemplate =
           "<span class=\"glyphicon glyphicon-ok-sign\" aria-hidden=\"true\"></span> " +
           "<span class=\"gathering-status-achieved\">{{gatheringSettings.discountPrice}}円引き達成!!!</span><br>" +
           "<span class=\"gathering-status-current-orders\">現在 {{orderStat.numOrders}}個</span>";

        var linker = function(scope, element, attrs) {

            var doCompile = function() {
                var template = defaultTemplate;
                if (scope.gatheringSettings.isAchieved(scope.orderStat.numOrders)) {
                    template = achievedTemplate;
                }

                element.html(template).show();

                $compile(element.contents())(scope);
            };

            scope.$watch('orderStat', doCompile);
            scope.$watch('gatheringSettings', doCompile);
        };

        return {
            restrict: "A",
            link: linker,
            scope: {
                gatheringSettings: "=",
                orderStat: "="
            }
        };
    }

})();
