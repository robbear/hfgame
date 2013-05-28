//
// Module dependencies
//
var express = require('express'),
    connect = require('connect'),
    router = require('./routes/router'),
    uuid = require('node-uuid'),
    logger = require('./logger/logger'),
    locale = require('locale'),
    supportedLanguages = ["en", "en-US"],
    hfConfig = require('./config/config');

var app = module.exports = express(locale(supportedLanguages));

logger.bunyanLogger().info("***** Starting hfgame.com web application *****");

//
// Configuration
//
app.configure(function() {
    app.set('view engine', 'html');
    app.use(express.bodyParser());
    app.use(express.methodOverride());
    app.use(express.favicon(__dirname + '/public/favicon.ico'));
    if (hfConfig.logStaticResources) {
        app.use(logRequest);
    }
    app.use(express.logger({ format: ':response-time ms - :date - :req[x-real-ip] - :method :url :user-agent / :referrer' }));
    app.use(express.static(__dirname + '/public'));
    if (!hfConfig.logStaticResources) {
        app.use(logRequest);
    }
    app.use(app.router);
    app.use(logError);
 });

function logRequest(req, res, next) {
    req.req_id = uuid.v4();

    logger.bunyanLogger().info({req: req}, "REQUEST");
    next();
}

function logError(req, res, next) {
    logger.bunyanLogger().error({err: err, req: req});
    console.log(err.stack);

    // Route to error handler page
    router.handleError(req, res);
}

/*
app.configure('development', function() {
    app.use(express.errorHandler({ dumpExceptions: true, showStack: true }));
});

app.configure('production', function() {
    app.use(express.errorHandler());
});
*/

//
// Entry point for all router requests
//
app.get('*', function(req, res, next) {
    //
    // Map http://www.host.com/foo/bar to http://host.com/foo/bar
    //
    if(req.host.match('www.')) {
        var index = req.host.indexOf('www.');
        var newHost = req.host.substring(index + 4);
        var protocol = (req.header('X-Forwarded-Protocol') == 'https') ? 'https://' : 'http://';
        var newUrl = protocol + newHost + req.url;
        res.writeHead(301, { 'Location': newUrl, 'Expires': (new Date).toGMTString() });
        res.end();
    }
    else {
        return next();
    }
});

//
// Routes - see routes/router.js
//
for (var i = 0; i < router.routeMap.length; i++) {
    var route = router.routeMap[i];
    if (route.serverHandler) {
        app.get(route.route, route.serverHandler);
    }
}

// 404 and other errors
app.get('/notfound', router.handle404);
app.get('/error', router.handleError);
app.get('*', router.handle404);

app.listen(process.env.PORT || 1337);
console.log("NodeWebSite server listening on port %s in %s mode", process.env.PORT || 1337, hfConfig.environment);
console.log("Using node.js %s, connect %s, Express %s", process.version, connect.version, express.version);
logger.bunyanLogger().info("NodeWebSite server listening on port %s in %s mode", process.env.PORT || 1337, hfConfig.environment);
logger.bunyanLogger().info("Using node.js %s, connect %s, Express %s", process.version, connect.version, express.version);