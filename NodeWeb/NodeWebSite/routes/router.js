
// Includes
var cons = require('consolidate'),
    express = require('express'),
    connect = require('connect'),
    hogan = require('hogan.js'),
    fs = require('fs'),
    engineName = 'hogan';

var appTemplates = {};

// Pre-compile templates
initTemplates();


// This function sends the actual http response and makes sure it gets logged by Bunyan
function sendResponse(res, data) {
    res.send(data);
}

//
// Initialize the routeMap which will be built up in the code below
//
exports.routeMap = [];


exports.handle404 = function(req, res) {
    res.status(404);
    if (req.accepts('html')) {
        sendOutputHtml(req, res, 'views/404_header.html', 'views/404_body.html',
            {
                "PageTitle": "Not found"
            });
        return;
    }

    if (req.accepts('json')) {
        sendResponse(res, {error: 'Not found'});
        return;
    }

    res.type('txt');
    sendResponse(res, 'Not found');
};

exports.handleError = function (req, res) {
    res.status(500);
    if (req.accepts('html')) {
        sendOutputHtml(req, res, 'views/500_header.html', 'views/500_body.html',
            {
                "PageTitle": "Error"
            });
        return;
    }

    if (req.accepts('json')) {
        sendResponse(res, {error: 'Error'});
        return;
    }

    res.type('txt');
    sendResponse(res, 'Error');
};


//
// Define the routes and exports for all URLs.
//
// Each item contains a name for the export, header and body HTML for standard template pages, and the page title.
//
// The routeFn specifies what route function export to use. If null, it will use the standard route function
// defined in AddRouteExport.
//
//
var routeExports = [
    {
        exportName: "index",

        // path to template file to be included inside <head> element
        header: "views/index_header.html",

        // path to template to be included inside <body>. This template will contain page content.
        body: "views/index_body.html",

        // Page title displayed in browser header
        pageTitle: "HF Game",

        // Optional custom route function
        routeFn: null,
        routeMap: {
            route: '/'
        }
    },
    {
        exportName: "test",
        header: "views/teste_header.html",
        body: "views/test_body.html",
        pageTitle: "HF Game Test Page",
        routeFn: null,
        routeMap: {
            route: '/test'
        }
    }
];

//
// We instantiate the exports in this loop. Since the standard route exports definitions involve anonymous (lambda) functions,
// we have an issue of Javascript closure, so we have to be careful scope-wise how we pass the parameters to the
// lambda functions. Observe how for routeFn==null, the header, body, and pageTitle parameters are passed into
// the subsequent lambda through Javascript closure.
//
for (var i = 0; i < routeExports.length; i++) {
    addRouteExport(routeExports[i].exportName, routeExports[i].header, routeExports[i].body, routeExports[i].pageTitle,
        routeExports[i].routeFn, routeExports[i].routeMap, routeExports[i].footerContent, routeExports[i].navigationCategory);
}

function addRouteExport(exportName, header, body, pageTitle, routeFn, routeMap) {
    if (routeFn) {
        // Use the custom route handler specified
        exports[exportName] = routeFn;
    }
    else {
        // Default route handler function
        exports[exportName] = function (req, res) {

            var propertyBag = {
                "PageTitle": pageTitle
            };

            sendOutputHtml(req, res, header, body, propertyBag);
        };
    }

    // Build a route map item to add to the exports.routeMap array
    var routeMapItem = {};
    routeMapItem.route = routeMap.route;
    routeMapItem.serverHandler = exports[exportName];

    // Extend the exports.routeMap array with the new item
    exports.routeMap.push(routeMapItem);
}


// Pre-compiles hogan templates and makes them accessible within the appTemplates global.
function initTemplates() {
    fs.readFile('views/layout.html', 'utf-8', function(err, layoutString) {
        appTemplates.wrapper = hogan.compile(layoutString.toString('utf-8'));
    });
}

//
// Helpers
//

function sendOutputHtml(req, res, headerContentPath, bodyContentPath, propertyBag) {

    var fnReadBody = function (bodyContentPath, headerContentString) {
        fs.readFile(bodyContentPath, 'utf-8', function (err, bodyContentString) {
            var partials = {
                "HeaderContent": headerContentString,
                "BodyContent": bodyContentString
            };
            var outputHtml = appTemplates.wrapper.render(propertyBag, partials);

            sendResponse(res, outputHtml);
        });
    };

    var fnReadHeader = function (headerContentPath, bodyContentPath) {
        fs.readFile(headerContentPath, 'utf-8', function (err, headerContentString) {
            fnReadBody(bodyContentPath, headerContentString);
        });
    };

    // Define this helper function to be used as a callback in the alternate if/else
    // code paths below. Remember that everything needs to stay async.
    var fnSendOutput = function(headerContentPath, bodyContentPath) {
        fnReadHeader(headerContentPath, bodyContentPath);
    };

    fnSendOutput(headerContentPath, bodyContentPath);
}
