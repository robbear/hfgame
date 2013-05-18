var restify = require('restify'),
    fs = require('fs'),
    testController = require('./controllers/TestController.js'),
    usersController = require('./controllers/UsersController.js'),
    userLocationsController = require('./controllers/UserLocationsController.js'),
    logger = require('../logger/logger');

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
        api.returns = route.returns;

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
        parameters: '{}',
        returns: "API list"
    },
    {
        httpVerb: 'get',
        exportName: 'test',
        routeFn: testController.testRoute,
        route: '/test',
        description: 'Sample test api. Uses HTTP GET. Returns a bogus string.',
        parameters: '{}',
        returns: "{ REST: ok, Database: ok }"
    },
    {
        httpVerb: 'post',
        exportName: 'User_createUser',
        routeFn: usersController.createUser,
        route: '/users/createuser',
        description: "Create a new user account. Parameters are passed in the form body",
        parameters: "{username: 'username', password: 'password'}",
        returns: "{ id: UserId, username: userName }"
    },
    {
        httpVerb: 'get',
        exportName: 'User_login',
        routeFn: usersController.login,
        route: '/users/login',
        description: "Log in to an existing account",
        parameters: "{username: 'username', password: 'password'}",
        returns: "{ id: UserId, username: userName }"
    },
    {
        httpVerb: 'post',
        exportName: 'UserLocation_createUserLocation',
        routeFn: userLocationsController.createUserLocation,
        route: '/userlocations/createlocation',
        description: "Create a user location. Parameters are passed in the form body.",
        parameters: "{userId: userId, coordinates: [longitude, latitude], altitude: altitude, accuracy: accuracy, date: date}",
        returns: "{}"
    },
    {
        httpVerb: 'post',
        exportName: 'UserLocation_insertUserLocations',
        routeFn: userLocationsController.insertUserLocations,
        route: '/userlocations/createlocations',
        description: "Create a collection of user locations. Parameters are passed in the form body.",
        parameters: "{userId: userId, locations: [{coordinates: [longitude, latitude], altitude: altitude, accuracy: accuracy, date: date}]}",
        returns: "{}"
    },
    {
        httpVerb: 'get',
        exportName: 'UserLocation_getUserLocationsByDate',
        routeFn: userLocationsController.getUserLocationsByDate,
        route: '/userlocations/bydate',
        description: "Get the locations for a user by specifying a range of dates.",
        parameters: "{userId: 'userId', start: 'startDate', end: 'endDate', limit: 'max returned items'}",
        returns: "[{ coordinates: [lon, lat], altitude: altitude, accuracy: accuracy, date: date }]"
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
