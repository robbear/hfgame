var cons = require('consolidate'),
    express = require('express'),
    connect = require('connect'),
    logger = require('../logger/logger'),
    hfConfig = require('../config/config'),
    hogan = require('hogan.js'),
    fs = require('fs'),
    engineName = 'hogan';

var timestampString = "uninitialized",
    appTemplates = {};

// Pre-compile templates
initTemplates();

// Read in Timestamp file used in cache busting
fs.readFile('timestamp.txt', 'utf-8', function (err, ts) {
    if (ts) {
        timestampString = ts.trim();
    }
});

// This function sends the actual http response and makes sure it gets logged by Bunyan
function sendResponse(res, data) {
    res.send(data);
    logger.bunyanLogger().info({res: res}, "RESPONSE");
}

// Cache targeted pages for 15 minutes
exports.cacheSeconds = 60 * 15;

//
// Initialize the routeMap which will be built up in the code below
//
exports.routeMap = [];

//
// Route exports expected by server.js
//
exports.handle404 = function(req, res) {
    res.status(404);
    if (req.accepts('html')) {
        sendOutputHtml("root", "handle404", null, req, res, 'views/404_header.html', 'views/404_body.html',
            {
                "PageTitle": "Not found"
            });
        return;
    }

    if (req.accepts('json')) {
        res.send({ error: 'Not found' });
        return;
    }

    res.type('txt');
    sendResponse(res, "Not found");
};

exports.handleError = function (req, res) {
    res.status(500);
    if (req.accepts('html')) {
        sendOutputHtml("root", "handleError", null, req, res, 'views/500_header.html', 'views/500_body.html',
            {
                "PageTitle": "Error"
            });
        return;
    }

    if (req.accepts('json')) {
        res.send({ error: 'Error' });
        return;
    }

    res.type('txt');
    sendResponse(res, "Error");
};

//
// Customized, non-standard route functions
//
var versionRoute = function (req, res) {
    var appVersion = "Unspecified";
    var angularVersion = "";
    fs.readFile('public/lib/angular/version.txt', 'utf-8', function (err, versionString) {
        if (versionString) {
            angularVersion = versionString.trim();
        }
        fs.readFile('version.txt', 'utf-8', function (err, versionString) {
            if (versionString) {
                appVersion = versionString.trim();
            }

            sendResponse(res, {
                app: appVersion,
                buildstring: timestampString,
                nodejs: process.version,
                connect: connect.version,
                express: express.version,
                angular: angularVersion
            });
        });
    });
};

//
// Define the routes and exports for all URLs.
//
// Each item contains a name for the export, header and body HTML for standard template pages, and the page title.
//
// The routeFn specifies what route function export to use. If null, it will use the standard route function
// defined in AddRouteExport.
//
// The routeMap subobject defines the URL mapping and a clientObj object for defining the corresponding
// Angular controller for SPA client views on the route. From the routeMap, we build both the server-side
// exports.routeMap array, and the client-side routeMap array.
//
var routeExports = [
    {
        master: "root",
        exportName: "index",
        header: "views/index_header.html",
        body: "views/index_body.html",
        pageTitle: "Hyperfine Software",
        routeFn: null,
        routeMap: {
            route: '/',
            clientObj: {templateUrl: 'generic.html', controller: 'HomeCtrl'}
        }
    },
    {
        master: "root",
        exportName: "contact",
        header: "views/contact_header.html",
        body: "views/contact_body.html",
        pageTitle: "Hyperfine Software - Contact",
        routeFn: null,
        routeMap: {
            route: '/contact',
            clientObj: {templateUrl: 'generic.html', controller: 'ContactCtrl'}
        }
    },
    {
        master: null,
        exportName: "version",
        header: null,
        body: null,
        pageTitle: null,
        routeFn: versionRoute,
        routeMap: {
            route: '/version',
            clientObj: null
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
    addRouteExport(routeExports[i].master, routeExports[i].exportName, routeExports[i].templateCallback, routeExports[i].header, routeExports[i].body, routeExports[i].pageTitle,
        routeExports[i].routeFn, routeExports[i].routeMap);
}

function addRouteExport(masterFile, exportName, templateCallback, header, body, pageTitle, routeFn, routeMap) {
    if (routeFn) {
        // Use the custom route handler specified
        exports[exportName] = routeFn;
    }
    else {
        // Default route handler function
        exports[exportName] = function (req, res) {
            var propertyBag = { "PageTitle": pageTitle };

            sendOutputHtml(masterFile, exportName, templateCallback, req, res, header, body, propertyBag);
        }
    }

    // Build a route map item to add to the exports.routeMap array
    var routeMapItem = {};
    routeMapItem.route = routeMap.route;
    routeMapItem.serverHandler = exports[exportName];
    routeMapItem.clientObj = routeMap.clientObj;

    // Extend the exports.routeMap array with the new item
    exports.routeMap.push(routeMapItem);
}

// Pre-compiles hogan templates and makes them accessible within the appTemplates global
function initTemplates() {
    fs.readFile('views/layout.html', 'utf-8', function(err, layoutString) {
        appTemplates.wrapper = hogan.compile(layoutString.toString('utf-8'));
    });
}

//
// Helpers
//

function sendOutputHtml(master, exportName, callback, req, res, headerContentPath, bodyContentPath, propertyBag) {
    var clientRouteMap = 'hfdotcomApp.clientRouteMap=' + JSON.stringify(createClientRouteMap()) + ';';
    propertyBag.ClientRouteMap = ""; /*clientRouteMap;*/ // BUGBUG - stub out for now
    propertyBag.EnableClientLogging = hfConfig.isClientLoggingEnabled();
    propertyBag.TimeStamp = timestampString;

    if (callback) {
        callback(master, exportName, req, res, propertyBag);
    }

    var templateWrapper;
    if (!master || master == "root") {
        templateWrapper = appTemplates.wrapper;
    }
    else if (master == "rfi") {
        templateWrapper = appTemplates.rfi_wrapper;
    }
    else if (master == "ct") {
        templateWrapper = appTemplates.ct_wrapper;
    }

    res.setHeader('Cache-Control', 'public,max-age=' + exports.cacheSeconds);
    res.setHeader('ETag', timestampString);

    var fnReadBody = function (bodyContentPath, headerContentString) {
        fs.readFile(bodyContentPath, 'utf-8', function(err, bodyContentString) {
                var partials = { "HeaderContent": headerContentString, "BodyContent": bodyContentString };
                var outputHtml = templateWrapper.render(propertyBag, partials);

                res.send(outputHtml);
        });
    };

    var fnReadHeader = function (headerContentPath, bodyContentPath) {
        fs.readFile(headerContentPath, 'utf-8', function(err, headerContentString) {
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

function createClientRouteMap() {
    var clientRouteMap = [];

    for (var i = 0; i < exports.routeMap.length; i++) {
        var route = exports.routeMap[i];
        if (!route.clientObj) {
            // No clientObj defined? Skip this item.
            continue;
        }

        var item = {};
        item.route = route.route;
        item.clientObj = route.clientObj;

        clientRouteMap.push(item);
    }

    return clientRouteMap;
}
