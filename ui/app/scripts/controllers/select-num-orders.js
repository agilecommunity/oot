(function(){

    angular.module('MyControllers')
        .controller('SelectNumOrdersController', SelectNumOrdersController);

    SelectNumOrdersController.$inject = ['$scope'];

    function SelectNumOrdersController($scope) {

        var vm = this;

        vm.select_this = function(value) {
            $scope.$close(value);
        };
    }

})();
