
angular.module('MyServices')
.factory('RouteFinder',
    ['$route', '$location',         // $routeを見つけ出すサービス
    function ($route, $location) {  // angular-route.jsからコピーしたもの

    var inherit = function (parent, extra) {
        return angular.extend(new (angular.extend(function () {
        }, {prototype: parent}))(), extra);
          };

          /**
           * @param on {string} current url
           * @param route {Object} route regexp to match the url against
           * @return {?Object}
           *
           * @description
           * Check if the route matches the current url.
           *
           * Inspired by match in
           * visionmedia/express/lib/router/router.js.
           */
          var switchRouteMatcher = function (on, route) {
              var keys = route.keys,
                  params = {};

              if (!route.regexp) return null;

              var m = route.regexp.exec(on);
              if (!m) return null;

              for (var i = 1, len = m.length; i < len; ++i) {
                  var key = keys[i - 1];

                  var val = 'string' == typeof m[i] ?
                      decodeURIComponent(m[i])
                      : m[i];

                  if (key && val) {
                      params[key.name] = val;
                  }
              }
              return params;
          };

          return {
              /**
               * @returns {Object} the current active route, by matching it against the URL
               */
              parseRoute: function () {
                  // Match a route
                  var params, match;
                  angular.forEach($route.routes, function (route, path) {
                      if (!match && (params = switchRouteMatcher($location.path(), route))) {
                          match = inherit(route, {
                              params: angular.extend({}, $location.search(), params),
                              pathParams: params});
                          match.$$route = route;
                      }
                  });
                  // No route matched; fallback to "otherwise" route
                  return match || routes[null] && inherit(routes[null], {params: {}, pathParams: {}});
              }
          };
}]);
