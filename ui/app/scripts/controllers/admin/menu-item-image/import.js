
angular.module('MyControllers')
    .controller('MenuItemImageImportController',
    ['$scope', '$location', '$routeParams', '$filter', 'User', 'MenuItem', 'DailyMenu',
        function ($scope, $location, $routeParams, $filter, User, MenuItem, DailyMenu) {

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

        $scope.upload_data.submit()
        .done(function( data, textStatus, jqXHR ) {
            bootbox.alert("登録が完了しました", function () {
                $scope.clearForm();
            });
        })
        .fail(function( jqXHR, textStatus, errorThrown ) {
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