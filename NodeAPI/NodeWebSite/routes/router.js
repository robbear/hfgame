var restify = require('restify'),
    fs = require('fs'),
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
}

var testRoute = function (req, res, next) {
    res.send({msg: 'testRoute value'});
    next();
}

var testItemRoute = function (req, res, next) {
    res.send({name: req.params.item, value: "Don't you wish we had a database?"});
    next();
}

var testPutRoute = function (req, res, next) {
    res.send({name: req.params.name, age: req.params.age});
    next();
}


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
        routeFn: testRoute,
        route: '/test',
        description: 'Sample test api. Uses HTTP GET. Returns a bogus string.',
        parameters: []
    },
    {
        httpVerb: 'get',
        exportName: 'testItem',
        routeFn: testItemRoute,
        route: '/testitem/:item',
        description: 'Sample test api with parameter. Uses HTTP GET. Returns a simulated fetched item.',
        parameters: [{'item': 'Item identifier'}]
    },
    {
        httpVerb: 'put',
        exportName: 'testPut',
        routeFn: testPutRoute,
        route: '/testput',
        description: "Sample put call with parameters. Uses HTTP PUT. Parameters set in the request body: 'name=foo&age=21'. Returns an echo of the name/age parameters sent.",
        parameters: [{'name': "Person's name"}, {'age': "Person's age"}]
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


