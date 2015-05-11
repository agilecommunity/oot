(function(){

    angular.module('MyControllers')
        .controller('MenuItemEditController', MenuItemEditController);

    MenuItemEditController.$inject = ['$scope', '$location', '$filter', 'MyDialogs', 'MenuItem', 'menuItem'];

    function MenuItemEditController($scope, $location, $filter, MyDialogs, MenuItem, menuItem) {

        var vm = this;

        function createItem(menuItem) {

            var handler = {};

            handler.success = function(saved) {
                $scope.$close(saved[0]);
            };

            handler.error = function(error) {
                console.log(error);
                if (error.status == 422) {
                    vm.errors = error.data.errors[0];
                } else {
                    var dialog = MyDialogs.error("データ登録・更新失敗", error.data.message);

                    dialog.result["finally"](function(){
                        $scope.$close();
                    });
                }
            };

            MenuItem.create({}, [menuItem], handler.success, handler.error);
        }

        function updateItem(menuItem) {

            var handler = {};

            handler.success = function(saved) {
                $scope.$close(saved);
            };

            handler.error = function(error) {
                console.log(error);
                if (error.status === 422) {
                    vm.errors = error.data.errors;
                } else {
                    var dialog = MyDialogs.error("データ登録・更新失敗", error.data.message);

                    dialog.result["finally"](function(){
                        $scope.$close();
                    });
                }
            };

            menuItem.$update({}, handler.success, handler.error);
        }

        function setImage(canvas, src) {
            var context = canvas.getContext("2d");

            var image = new Image();
            image.src = src;

            if (src.length > 512000) {
                MyDialogs.error("画像貼り付け", "貼り付ける画像は512kb以下にしてください 現在のサイズ: " + src.length );
                return;
            }

            image.onload = function(){
                canvas.width = image.width;
                canvas.height = image.height;
                context.drawImage(image, 0, 0);

                if (src.lastIndexOf("data:image", 0) === 0) {
                    vm.menuItem.image = image.src.replace("data:image/png;base64,", "");
                }
            };
        }

        function waitforpastedata (elem, savedcontent) {
            if (elem.childNodes && elem.childNodes.length > 0) {
                processpaste(elem, savedcontent);
            }
            else {
                that = {
                    e: elem,
                    s: savedcontent
                };
                that.callself = function () {
                    waitforpastedata(that.e, that.s);
                };
                setTimeout(that.callself,20);
            }
        }

        function processpaste (elem, savedcontent) {
            var pasteddata = elem.innerHTML;

            if (pasteddata.lastIndexOf('<img src=', 0) === 0) {
                var canvas = angular.element("#menuItem\\.image")[0];
                var pastedImage = angular.element(pasteddata);

                setImage(canvas, pastedImage.attr("src"));
            }
        }

        vm.menuItem = menuItem;
        var menuItemSaved = angular.copy(vm.menuItem);

        vm.errors = [];

        vm.save = function() {
            if (vm.menuItem.id === undefined) {
                createItem(vm.menuItem);
            } else {
                updateItem(vm.menuItem);
            }
        };

        vm.cancel = function() {
            angular.copy(menuItemSaved, vm.menuItem);
            $scope.$dismiss();
        };

        vm.setCategory = function(value) {
            vm.menuItem.category = value;
        };

        vm.setStatus = function(value) {
            vm.menuItem.status = value;
        };

        vm.hasError = function(name) {
            if (vm.errors.length === 0) {
                return false;
            }

            return (vm.errors[name] !== undefined && vm.errors[name] !== null);
        };

        vm.canPaste = function() {
            // ひとまずFirefoxのみ対応可能としておく
            var userAgent  = window.navigator.userAgent.toLowerCase();

            if (userAgent.indexOf('msie') != -1) {
                return false;
            } else if (userAgent.indexOf('trident/7') != -1) {
                return false;
            } else if (userAgent.indexOf('firefox') != -1) {
                return true;
            } else if (userAgent.indexOf('chrome') != -1) {
                return false;
            } else if (userAgent.indexOf('safari') != -1) {
                return false;
            } else {
                return false;
            }
        };

        vm.preparePaste = function() {
            console.log("preparePaste");
            angular.element("#pasteCather").html("");
            angular.element("#pasteCather").focus();
        };

        vm.startPaste = function(event) {
            console.log("startPaste");

            var elem = event.currentTarget;
            var savedcontent = elem.innerHTML;

            elem.innerHTML = "";
            delete vm.menuItem.image;
            waitforpastedata(elem, savedcontent);
        };

        vm.setImage = function() {
            var canvas = angular.element("#menuItem\\.image")[0];
            setImage(canvas, MenuItem.getImagePath(vm.menuItem));
        };
    }

})();
