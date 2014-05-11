define(['jquery',
        'angular',
        'moment',
        'angular.resource',
        'angular.route'],
function ($,
          angular,
          moment) {
    "use strict";

    var controllers = angular.module('MyControllers', []);

    controllers.controller('SigninController',
        ['$scope', '$location', 'User',
        function ($scope, $location, User) {
            $scope.signin = function () {
                User.signin($scope.user_email, $scope.user_password, {
                    success: function () {
                        var path = "/order";
                        if (User.current_user().is_admin === true) { // 管理者の場合は管理インデックスに飛ばす
                            path = "/admin/index";
                        }
                        $location.path(path);
                    },
                    error: function (status) {
                        alert("サインインに失敗しました。 status:" + status);
                    }
                });
            };
        }
    ]);

    controllers.controller('OrderController',
        ['$scope', '$location', '$filter', 'User', 'DailyMenu', 'DailyOrder',
        function ($scope, $location, $filter, User, DailyMenu, DailyOrder) {

        $scope.daily_menus = DailyMenu.queryByStatus({status: "open"},
            function (response) { // 成功時
                $scope.daily_orders = DailyOrder.query({},
                    function (response) { // 成功時
                    },
                    function (response) {   // 失敗時
                        alert("注文データが取得できませんでした。サインイン画面に戻ります。");
                        $location.path("/");
                    }
                );
            },
            function (response) {   // 失敗時
                alert("メニューのデータが取得できませんでした。サインイン画面に戻ります。");
                $location.path("/");
            }
        );

        $scope.order = function (daily_menu, daily_menu_item) { // イベントハンドラ

            // メニューの注文状況を切り替える
            var new_state = daily_menu_item.ordered !== true;
            daily_menu_item.ordered = new_state;

            // 注文オブジェクトがあるかどうかを調べる
            var order = $filter('getByOrderDate')($scope.daily_orders, daily_menu.menu_date);

            var reload_orders = function (request) {
                // 念のためサーバから最新の注文を取得する
                $scope.daily_orders = DailyOrder.query();
            };

            if (order !== null) {
                if (new_state === true) {
                    order.detail_items.push({menu_item: daily_menu_item.menu_item});
                } else {
                    order.detail_items = order.detail_items.filter(function (item, index) {
                        return (item.menu_item.id !== daily_menu_item.menu_item.id);
                    });
                }

                // あった場合は更新する
                if (order.detail_items.length > 0) {
                    order.$update({}, reload_orders);
                } else {
                    order.$delete({}, reload_orders);
                }
            } else {
                // ない場合は新しく作る
                var new_order = new DailyOrder();
                new_order.order_date = daily_menu.menu_date.unix() * 1000;
                new_order.local_user = User.current_user();
                new_order.detail_items = [
                    {menu_item: daily_menu_item.menu_item}
                ];

                DailyOrder.create({}, [new_order], reload_orders);
            }
        };

        $scope.totalPriceOfTheDay = function (target_date) {
            var order = $filter('getByOrderDate')($scope.daily_orders, target_date);
            if (order !== null) {
                return order.total_price();
            }
            return 0;
        };

        // メニューに注文状況を反映する
        var applyOrdered = function () {
            // メニューの注文状況をリセットする
            angular.forEach($scope.daily_menus, function (menu) {
                angular.forEach(menu.detail_items, function (item) {
                    item.ordered = false;
                });
            });
            // 注文を見ながらメニューの注文状況を変更する
            angular.forEach($scope.daily_orders, function (order) {
                var menu = $filter('getByMenuDate')($scope.daily_menus, order.order_date);
                if (menu !== null) {
                    angular.forEach(order.detail_items, function (o_d_item) {
                        angular.forEach(menu.detail_items, function (m_d_item) {
                            if (o_d_item.menu_item.id === m_d_item.menu_item.id) {
                                m_d_item.ordered = true;
                            }
                        });
                    });
                }
            });
        };

        // メニュー、または注文の内容が変わった場合は、メニューの注文状況を反映しなおす
        $scope.$watchCollection("daily_menus", applyOrdered);
        $scope.$watchCollection("daily_orders", applyOrdered);
    }]);

    controllers.controller('AdminIndexController',
        ['$scope', '$location', '$filter', 'User', 'DailyMenu', 'DailyOrder',
        function ($scope, $location, $filter, User, DailyMenu, DailyOrder) {

        $scope.daily_menus = DailyMenu.query({},
            function (response) { // 成功時
            // 何もしない
            },
            function (response) {   // 失敗時
                alert("メニューのデータが取得できませんでした。サインイン画面に戻ります。");
                $location.path("/");
            }
        );

        $scope.showChecklist = function (daily_menu) {
            $location.path("/admin/checklist/menu_date/" + daily_menu.menu_date.format('YYYY-MM-DD'));
        };

    }]);

    controllers.controller('AdminChecklistController',
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

    return controllers;

});