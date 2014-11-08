
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

        // 同じ商品を買った人は続けて表示されるよう、全商品の注文状況を文字列にまとめソートキーとする
        angular.forEach(checklist, function(check_item){
            var order_statuses_bits = "";
            angular.forEach($scope.daily_menu.detail_items, function(menu_item){
                if (check_item.order_statuses[menu_item.menu_item.id] !== undefined) {
                    order_statuses_bits += "0";
                } else {
                    order_statuses_bits += "1";
                }
            });
            check_item.order_statuses_bits = order_statuses_bits;
        });
        // 全商品の注文状況と、ユーザ名でソートする
        checklist = $filter('orderBy')(checklist, ["order_statuses_bits", "user_name"]);

        return checklist;
    };

    $scope.menu_date = moment.utc($routeParams.menu_date);

    var params = {menu_date: $scope.menu_date.format('YYYY-MM-DD')};

    DailyMenu.getByMenuDate(params,
        function (response) {
            // 店名、品名の順に並び替え
            response.detail_items = $filter('orderBy')(response.detail_items, ['menu_item.shop_name', 'menu_item.name']);
            $scope.daily_menu = response;

            $scope.daily_orders = DailyOrder.getByOrderDate({order_date: $scope.menu_date.format('YYYY-MM-DD')},
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

