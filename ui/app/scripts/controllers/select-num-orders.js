(function(){

    angular.module('MyControllers')
        .controller('SelectNumOrdersController', SelectNumOrdersController);

    SelectNumOrdersController.$inject = ['$scope'];

    function SelectNumOrdersController($scope) {

        var vm = this;

        vm.selectThis = function(value) {
            console.log(value);
            $scope.$close(value);
        };
    }

})();
