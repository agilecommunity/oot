(function(){

    angular.module('MyControllers')
        .controller('UserLinksController', UserLinksController);

    UserLinksController.$inject = ['$location', 'User'];

    function UserLinksController($location, User) {

        var vm = this;

        vm.isSignedIn = function() {
            return User.isSignedIn();
        };

        vm.signout = function() {
            var handler = function() {
                $location.path("/");
            };
            User.signout({ success: handler, error: handler });
        };
    }

})();