
angular.module('MyControllers')
    .controller('MenuItemImageImportController',
    ['$scope', '$location', '$routeParams', '$filter', 'usSpinnerService', 'User', 'MenuItem', 'DailyMenu',
        function ($scope, $location, $routeParams, $filter, usSpinnerService, User, MenuItem, DailyMenu) {

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

            if (data !== "" || data[0] !== undefined) {
                result = $.parseJSON(data[0].body.innerText);
            }

            stopBlock();

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
            stopBlock();

            bootbox.alert("登録できませんでした status:" + errorThrown, function () {
                $scope.clearForm();
            });
        });
    };

    $scope.clearForm = function() {
        $scope.uploadData = null;
        $scope.menuItemImagesForm.$setPristine();
        $scope.$apply();
    };

    }]
);