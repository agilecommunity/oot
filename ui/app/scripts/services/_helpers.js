
// angularのコードからコピペ
app.my.helpers.transformRequestDefault = function(data) {
    var JSON_START = /^\s*(\[|\{[^\{])/;
    var JSON_END = /[\}\]]\s*$/;
    var PROTECTION_PREFIX = /^\)\]\}',?\n/;
    if (angular.isString(data)) {
        // strip json vulnerability protection prefix
        data = data.replace(PROTECTION_PREFIX, '');
        if (JSON_START.test(data) && JSON_END.test(data))
            data = angular.fromJson(data);
    }
    return data;
};
