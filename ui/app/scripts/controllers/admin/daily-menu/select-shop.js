angular.module('MyControllers')
    .controller('DailyMenuSelectShopController',
    ['$scope', '$location', '$routeParams', '$filter', '$modal', 'User', 'Shop',
        function ($scope, $location, $routeParams, $filter, $modal, User, Shop) {

            $scope.shops = Shop.query({},
                function (response) { // 成功時
                    // 何もしない
                },
                function (response) {   // 失敗時
                    alert("データが取得できませんでした。サインイン画面に戻ります。");
                    $location.path("/");
                });

            $scope.selectThis = function(item) {
                $scope.$close(item);
            };
        }]);