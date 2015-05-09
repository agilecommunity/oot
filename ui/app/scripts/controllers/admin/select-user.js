(function(){

    angular.module('MyControllers')
        .controller('SelectUserController', SelectUserController);

    SelectUserController.$inject = ['$scope', '$location', 'User'];

    function SelectUserController($scope, $location, User) {

        var vm = this;

        vm.users = User.query({},
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
