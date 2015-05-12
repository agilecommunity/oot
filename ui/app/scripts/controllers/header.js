(function() {

    angular.module('MyControllers')
        .controller('HeaderController', HeaderController);

    HeaderController.$inject = ['$location'];

    function HeaderController($location) {
        var vm = this;

        //---- ヘルパ
        vm.isActive = function(url) {
            return $location.path().lastIndexOf(url, 0) === 0;
        };
    }

})();
