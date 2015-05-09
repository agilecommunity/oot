(function(){

    angular.module('MyServices')
        .factory('MyDialogs', MyDialogs);

    MyDialogs.$inject = ['dialogs'];

    function MyDialogs(dialogs) {

        var MyClass = {};
        angular.extend(MyClass, dialogs);

        MyClass.resolveError = function(header, reason, opts) {
            var addMessages = [];

            if (reason.url) {
                addMessages.push("URL: " + reason.url);
            }

            if (reason.status) {
                addMessages.push("ステータスコード: " + reason.status);
            }

            if (reason.reason) {
                addMessages.push("理由: " + reason.reason);
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