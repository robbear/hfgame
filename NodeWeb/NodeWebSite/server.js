//
// Module dependencies
//
var express = require('express'),
    cons = require('consolidate'),
    connect = require('connect'),
    router = require('./routes/router');

var app = module.exports = express();

//
// Configuration
//
app.configure(function() {
    app.set('view engine', 'html');
    app.use(express.bodyParser());
    app.use(express.methodOverride());
    // stdout request logging. Useful for development.
    app.use(express.logger({ format: ':response-time ms - :date - :req[x-real-ip] - :method :url :user-agent / :referrer' }));
    app.use(express.static(__dirname + '/public'));
    app.use(app.router);
    app.use(express.favicon(__dirname + '/public/favicon.ico'));
});

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
        res.writeHead(301, { 'Location': newUrl, 'Expires': (new Date()).toGMTString() });
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
console.log("NodeWebSite server listening on port %s in %s mode", process.env.PORT || 1337, app.settings.env);
console.log("Using node.js %s, connect %s, Express %s", process.version, connect.version, express.version);
