(function(){

    angular.module('MyControllers')
        .controller('SelectUserController', SelectUserController);

    SelectUserController.$inject = ['$scope', '$location', 'User', 'initialData'];

    function SelectUserController($scope, $location, User, initialData) {

        var vm = this;

        vm.users = initialData.users;

        vm.selectThis = function(item) {
            $scope.$close(item);
        };
    }

    app.my.resolvers.SelectUserController = {
        initialData: function($q, User) {

            var deferred = $q.defer();
            var initialData = {};

            User.query({
            }).$promise
            .then(function(value){
                initialData.users = value;
                deferred.resolve(initialData);
            })
            ["catch"](function(responseHeaders) {
                console.log(responseHeaders);
                deferred.reject({source: "resolve", url: responseHeaders.config.url, status: responseHeaders.status, reason: responseHeaders.data});
            });

            return deferred.promise;
        }
    };

})();
