var _silenceLogger = false;
var _databaseName = null;

exports.SilenceLogger = function(silence) {
    _silenceLogger = silence;
}

exports.SetDatabaseName = function(dbName) {
    _databaseName = dbName;
}

exports.StartServer = function(startserver_callback) {
    var restify = require('restify'),
        fs = require('fs'),
        router = require('./routes/router'),
        logger = require('./logger/logger'),
        hfConfig = require('./config/config.js');

    if (!_silenceLogger) logger.bunyanLogger().info("***** Starting hfapi web service *****");
    if (_databaseName) hfConfig.setDatabaseName(_databaseName);

    //
    // HTTP server options
    //
    var http_options = {
        name: "hfAPI HTTP",
        log: _silenceLogger ? null : logger.bunyanLogger()
    }

    //
    // HTTPS server options
    //
    var https_options = hfConfig.usesHttps() ? {
        name: "hfAPI HTTPS",
        key: fs.readFileSync('./certificates/ssl/self-signed/server.key'),
        certificate: fs.readFileSync('./certificates/ssl/self-signed/server.crt'),
        log: _silenceLogger ? null : logger.bunyanLogger()
    } : null;


    // Instantiate the HTTP and HTTPS servers
    var server = restify.createServer(http_options);
    var https_server = hfConfig.usesHttps() ? restify.createServer(https_options) : null;


    //
    // Pre-router callback.
    //
    function preRoutingHandler(req, res, next) {
        return next();
    }

    server.use(restify.acceptParser(server.acceptable));
    server.use(restify.authorizationParser());
    server.use(restify.dateParser());
    server.use(restify.queryParser());
    server.use(restify.jsonp());
    server.use(restify.gzipResponse());
    server.use(restify.bodyParser());
    // Check userAgent for curl. If it is, this sets the Connection header
    // to "close" and removes the "Content-Length" header.
    server.pre([restify.pre.userAgentConnection(), preRoutingHandler]);

    if (hfConfig.usesHttps()) {
        https_server.use(restify.acceptParser(server.acceptable));
        https_server.use(restify.authorizationParser());
        https_server.use(restify.dateParser());
        https_server.use(restify.queryParser());
        https_server.use(restify.jsonp());
        https_server.use(restify.gzipResponse());
        https_server.use(restify.bodyParser());
        https_server.pre([restify.pre.userAgentConnection(), preRoutingHandler]);
    }

    //
    // Routes - see routes/router.js
    //
    for (var i = 0; i < router.routeMap.length; i++) {
        var route = router.routeMap[i];

        server[route.httpVerb](route.route, route.serverHandler);
        if (hfConfig.usesHttps()) {
            https_server[route.httpVerb](route.route, route.serverHandler);
        }
    }

    //
    // Connect to the database
    //
    var connectionString = hfConfig.connectionString();
    hfConfig.dbUtils().connectToMongoDB(connectionString, function(err) {
        if (err) {
            console.log('Failed to open database: ' + err.message);
            throw ('Failed to open database: ' + err.message);
        }

        //
        // Start the servers on the appropriate ports
        //
        server.listen(process.env.PORT || 1338 , function() {
            if (!_silenceLogger) console.log('%s listening at %s', server.name, server.url);

            if (hfConfig.usesHttps()) {
                https_server.listen(443, function() {
                    if (!_silenceLogger) console.log('%s listening at %s', https_server.name, https_server.url);

                    if (startserver_callback) {
                        startserver_callback();
                    }
                });
            }
            else {
                if (startserver_callback) {
                    startserver_callback();
                }
            }
        });
    });
};
