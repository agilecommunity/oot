
angular.module('MyControllers')
    .controller('MenuItemNewController',
    ['$scope', '$location', '$routeParams', '$filter', 'User', 'MenuItem', 'DailyMenu',
        function ($scope, $location, $routeParams, $filter, User, MenuItem, DailyMenu) {

    $('#menu-items').fileupload({
        add: function (ev, data) {
            if (data.files === null || data.files.length !== 1) {
                return;
            }
            var file = data.files[0];
            $scope.upload_file = file; // 複数選択は想定していない
            $scope.$apply();
        }
    });

    var empty_file = {name: 'ファイルを選択してください', size: null};

    $scope.upload_file = empty_file;

    $scope.uploadFileDescription = function() {
        var file = $scope.upload_file;
        if (file === empty_file) {
            return file.name;
        }
        var description = file.name;
        if (file.size !== undefined) {
            description += ' (' + file.size + ' bytes)';
        }
        return description;
    };

    $scope.bulkInsert = function() {
        if ($scope.upload_file === empty_file) {
            return;
        }
        $('#menu-items').fileupload('send', {files: [$scope.upload_file]})
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
        $scope.upload_file = empty_file;
        $scope.menuItemsForm.$setPristine();
        $scope.$apply();
    };

    }]
);