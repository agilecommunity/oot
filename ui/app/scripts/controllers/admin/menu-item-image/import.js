
angular.module('MyControllers')
    .controller('MenuItemImageImportController',
    ['$scope', '$location', '$routeParams', '$filter', 'dialogs', 'usSpinnerService', 'User', 'MenuItem', 'DailyMenu',
        function ($scope, $location, $routeParams, $filter, dialogs, usSpinnerService, User, MenuItem, DailyMenu) {

    var startBlock = function() {
        $.blockUI({baseZ: 2000, message: null});
        usSpinnerService.spin("spinner");
    };

    var stopBlock = function() {
        usSpinnerService.stop("spinner");
        $.unblockUI();
    };

    $('#menu-item-images').fileupload({
        add: function (ev, data) {
            if (data.files === null || data.files.length !== 1) {
                return;
            }
            $scope.uploadData = data;
            $scope.$apply();
        }
    });

    var emptyFile = {name: 'ファイルを選択してください', size: null};
    var getUploadFile = function() {
        if ($scope.uploadData === null) {
            return emptyFile;
        }

        return $scope.uploadData.files[0]; // 複数選択は想定していない
    };

    $scope.uploadData = null;
    $scope.uploadFile = emptyFile;

    $scope.uploadFileDescription = function() {
        var file = getUploadFile();
        if (file === emptyFile) {
            return file.name;
        }
        var description = file.name;
        if (file.size !== undefined) {
            description += ' (' + file.size + ' bytes)';
        }
        return description;
    };

    $scope.bulkImport = function() {
        if ($scope.uploadData === null) {
            return;
        }

        startBlock();

        $scope.uploadData.submit()
        .done(function( data, textStatus, jqXHR ) {
            var result = {statusCode: 200};

            if (data !== undefined && (data !== "" || data[0] !== undefined)) {
                result = $.parseJSON(data[0].body.innerText);
            }

            stopBlock();

            var dialog;

            if (result.statusCode === 200) {
                dialog = dialogs.notify("登録成功", "登録が完了しました。");
            } else {
                var messages = [
                    "登録できませんでした。",
                    "status:" + result.statusCode,
                    "message:" + result.message
                ];

                dialog = dialogs.error("登録失敗", messages.join("<br>"));
            }

            dialog.result["finally"](function(){
                $scope.clearForm();
            });
        })
        .fail(function( jqXHR, textStatus, errorThrown ) {
            stopBlock();

            var messages = [
                "登録できませんでした。",
                "status:" + jqXHR.status,
                "message:" + jqXHR.responseText
            ];

            var dialog = dialogs.error("登録失敗", messages.join("<br>"));

            dialog.result["finally"](function(){
                $scope.clearForm();
            });
        });
    };

    $scope.clearForm = function() {
        $scope.uploadData = null;
        $scope.menuItemImagesForm.$setPristine();
    };

    }]
);