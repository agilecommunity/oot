(function(){

    angular.module('MyControllers')
        .controller('DailyMenuSelectShopController', DailyMenuSelectShopController);

    DailyMenuSelectShopController.$inject = ['$scope', '$location', 'Shop'];

    function DailyMenuSelectShopController($scope, $location, Shop) {

        var vm = this;

        vm.shops = Shop.query({},
            function (response) { // 成功時
                // 何もしない
            },
            function (response) {   // 失敗時
                alert("データが取得できませんでした。サインイン画面に戻ります。");
                $location.path("/");
            });

        vm.selectThis = function(item) {
            $scope.$close(item);
        };
    }

})();
