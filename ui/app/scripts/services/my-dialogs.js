(function(){

    angular.module('MyServices')
        .factory('MyDialogs', MyDialogs);

    MyDialogs.$inject = ['dialogs'];

    function MyDialogs(dialogs) {

        var MyClass = {};
        angular.extend(MyClass, dialogs);

        MyClass.serverError = function(header, result, opts) {
            var addMessages = [];

            console.log(result);

            if (result.config && result.config.url) {
                addMessages.push("URL: " + result.config.url);
            }

            if (result.status) {
                addMessages.push("ステータスコード: " + result.status);
            }

            if (result.data) {
                if (angular.isString(result.data)) {
                    addMessages.push("理由: " + result.data);
                } else if (result.data.message) {
                    addMessages.push("理由: " + result.data.message);
                }
            }

            return MyClass.systemError(header, addMessages.join("<br>"), opts);
        };

        MyClass.systemError = function(header, additionalMsg, opts) {
            var messages = [
                "処理中にエラーが発生しました",
                "画面をリロードした後、再度操作を行ってみてください",
                "問題が解消しない場合は管理者に連絡してください",
                "",
                additionalMsg
            ];

            return dialogs.error(header, messages.join("<br>"), opts);
        };

        return MyClass;
    }

})();