
angular.module('MyControllers')
    .controller('MenuItemImageImportController',
    ['$scope', '$location', '$routeParams', '$filter', 'usSpinnerService', 'User', 'MenuItem', 'DailyMenu',
        function ($scope, $location, $routeParams, $filter, usSpinnerService, User, MenuItem, DailyMenu) {

    var start_block = function() {
        $.blockUI({baseZ: 2000, message: null});
        usSpinnerService.spin("spinner");
    };

    var stop_block = function() {
        usSpinnerService.stop("spinner");
        $.unblockUI();
    };

    $('#menu-item-images').fileupload({
        add: function (ev, data) {
            if (data.files === null || data.files.length !== 1) {
                return;
            }
            $scope.upload_data = data;
            $scope.$apply();
        }
    });

    var empty_file = {name: 'ファイルを選択してください', size: null};
    var getUploadFile = function() {
        if ($scope.upload_data === null) {
            return empty_file;
        }

        return $scope.upload_data.files[0]; // 複数選択は想定していない
    };

    $scope.upload_data = null;
    $scope.upload_file = empty_file;

    $scope.uploadFileDescription = function() {
        var file = getUploadFile();
        if (file === empty_file) {
            return file.name;
        }
        var description = file.name;
        if (file.size !== undefined) {
            description += ' (' + file.size + ' bytes)';
        }
        return description;
    };

    $scope.bulkImport = function() {
        if ($scope.upload_data === null) {
            return;
        }

        start_block();

        $scope.upload_data.submit()
        .done(function( data, textStatus, jqXHR ) {
            var result = {statusCode: 200};

            if (data !== "" || data[0] !== undefined) {
                result = $.parseJSON(data[0].body.innerText);
            }

            stop_block();

            if (result.statusCode === 200) {
                bootbox.alert("登録が完了しました", function () {
                    $scope.clearForm();
                });
            } else {
                bootbox.alert("登録できませんでした status:" + result.statusCode, function () {
                    $scope.clearForm();
                });
            }
        })
        .fail(function( jqXHR, textStatus, errorThrown ) {
            stop_block();

            bootbox.alert("登録できませんでした status:" + errorThrown, function () {
                $scope.clearForm();
            });
        });
    };

    $scope.clearForm = function() {
        $scope.upload_data = null;
        $scope.menuItemImagesForm.$setPristine();
        $scope.$apply();
    };

    }]
);