
angular.module('MyControllers')
.controller('AdminChecklistController',
    ['$scope', '$location', '$routeParams', '$filter', 'User', 'DailyMenu', 'DailyOrder',
    function ($scope, $location, $routeParams, $filter, User, DailyMenu, DailyOrder) {

    // チェックリストに使うデータの作成
    var create_checklist = function () {

        var checklist = [];

        // その日注文しているユーザごとに姓名と、注文状況を調査する
        angular.forEach($scope.daily_orders, function (order) {

            var checklist_item = [];
            checklist_item.user_name = order.local_user.last_name + " " + order.local_user.first_name;
            var order_statuses = [];

            angular.forEach(order.detail_items, function (item) {
                var order_status = [];
                order_status.menu_id = item.menu_item.id;
                order_status.ordered = true;
                order_statuses[item.menu_item.id] = order_status;
            });
            checklist_item.order_statuses = order_statuses;
            checklist.push(checklist_item);
        });

        return checklist;
    };

    $scope.menu_date = moment($routeParams.menu_date);

    var param_date = $scope.menu_date.format('YYYY-MM-DD');

    $scope.daily_menu = DailyMenu.getByMenuDate({menu_date: param_date},
        function (response) {
            $scope.daily_orders = DailyOrder.getByOrderDate({order_date: param_date},
                function (response) {
                    $scope.checklist = create_checklist();
                },
                function (response) {
                    if (response.status === 404) {
                        //FIXME: データが存在しなかった場合は戻らないと
                        return;
                    } else {
                        alert("注文のデータが取得できませんでした。");
                    }
                }
            );
        },
        function (response) {
            if (response.status === 404) {
                //FIXME: データが存在しなかった場合は戻らないと
                return;
            } else {
                alert("メニューのデータが取得できませんでした。");
            }
        }
    );
}]);

