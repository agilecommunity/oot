(function() {

    angular.module('MyServices')
        .factory('GatheringSetting', GatheringSetting);

    GatheringSetting.$inject = ['$resource', '$filter'];

    function GatheringSetting($resource, $filter) {
        var MyClass = $resource(
            '/api/v1.0/gathering-settings/:id',
            {id: "@id"}
        );

        MyClass.prototype.isAchieved = function(numOrders) {
            return (numOrders >= this.minOrders);
        };

        MyClass.createDummy = function() {
            var object = new MyClass();

            object.enabled = true;
            object.minOrders = 30;
            object.discountPrice = 20;

            return object;
        };

        return MyClass;
    }

})();
