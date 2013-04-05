var restify = require('restify'),
    fs = require('fs'),
    testController = require('./controllers/TestController.js'),
    usersController = require('./controllers/UsersController.js'),
    logger = require('../logger/logger'),
    hfConfig = require('../config/config.js');

//
// Initialize the routeMap which will be built up in the code below
//
exports.routeMap = [];


//
// Route functions
//
var apiRoute = function (req, res, next) {
    var apis = [];

    for (var i = 0; i < routeExports.length; i++) {
        var route = routeExports[i];
        var api = {};

        api.route = route.route;
        api.httpVerb = route.httpVerb;
        api.description = route.description;
        api.parameters = route.parameters;

        apis.push(api);
    }

    res.send(apis);
    next();
};

//
// Define the routes and exports for all APIs.
//
// Each item contains an http verb [del,get,head,opts,post,put,patch],
// an export name, a function for the route, and the route string.
//
var routeExports = [
    {
        httpVerb: 'get',
        exportName: 'api',
        routeFn: apiRoute,
        route: '/api',
        description: 'Returns a list of the currently supported APIs.',
        parameters: []
    },
    {
        httpVerb: 'get',
        exportName: 'test',
        routeFn: testController.testRoute,
        route: '/test',
        description: 'Sample test api. Uses HTTP GET. Returns a bogus string.',
        parameters: []
    },
    {
        httpVerb: 'get',
        exportName: 'testItem',
        routeFn: testController.testItemRoute,
        route: '/testitem/:item',
        description: 'Sample test api with parameter. Uses HTTP GET. Returns a simulated fetched item.',
        parameters: [{'item': 'Item identifier'}]
    },
    {
        httpVerb: 'put',
        exportName: 'testPut',
        routeFn: testController.testPutRoute,
        route: '/testput',
        description: "Sample put call with parameters. Uses HTTP PUT. Parameters set in the request body: 'name=foo&age=21'. Returns an echo of the name/age parameters sent.",
        parameters: [{'name': "Person's name"}, {'age': "Person's age"}]
    },
    {
        httpVerb: 'post',
        exportName: 'User_createUser',
        routeFn: usersController.createUser,
        route: '/users/createuser',
        description: "Create a new user account. Parameters are passed in the form body",
        parameters: [{'username': 'new username', 'password': 'new password'}]
    },
    {
        httpVerb: 'get',
        exportName: 'User_login',
        routeFn: usersController.login,
        route: '/users/login',
        description: "Log in to an existing account",
        parameters: [{'username': 'username'}, {'password': 'password'}]
    }
];

//
// We instantiate the exports in this loop
//
for (var i = 0; i < routeExports.length; i++) {
    addRouteExport(routeExports[i].httpVerb, routeExports[i].exportName, routeExports[i].routeFn, routeExports[i].route);
}

function addRouteExport(httpVerb, exportName, routeFn, route) {
    exports[exportName] = routeFn;

    // Build a route map item to add to the exports.routeMap array
    var routeMapItem = {};
    routeMapItem.httpVerb = httpVerb;
    routeMapItem.route = route;
    routeMapItem.serverHandler = exports[exportName];

    // Extend the exports.routeMap array with the new item
    exports.routeMap.push(routeMapItem);
}
