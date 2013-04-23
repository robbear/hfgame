var _databaseName = null;

exports.SetDatabaseName = function(dbName) {
    _databaseName = dbName;
};

exports.StartServer = function(startserver_callback, dbconnected_callback) {
    var restify = require('restify'),
        fs = require('fs'),
        router = require('./routes/router'),
        logger = require('./logger/logger'),
        hfConfig = require('./config/config.js');

    logger.bunyanLogger().info("%s***** Starting hfapi web service *****", hfConfig.tag());
    if (_databaseName) hfConfig.setDatabaseName(_databaseName);

    //
    // HTTP server options
    //
    var http_options = {
        name: "hfAPI HTTP",
        log: hfConfig.getRestifyLogging() ? logger.bunyanLogger() : null
    }

    //
    // HTTPS server options
    //
    var https_options = hfConfig.usesHttps() ? {
        name: "hfAPI HTTPS",
        key: fs.readFileSync('./certificates/ssl/self-signed/server.key'),
        certificate: fs.readFileSync('./certificates/ssl/self-signed/server.crt'),
        log: hfConfig.getRestifyLogging() ? logger.bunyanLogger() : null
    } : null;


    // Instantiate the HTTP and HTTPS servers
    var server = restify.createServer(http_options);
    var https_server = hfConfig.usesHttps() ? restify.createServer(https_options) : null;

    // Error handlers
    server.on('uncaughtException', function(req, res, route, err) {
        logger.bunyanLogger().error("%sUncaught Exception: %s on route %s", hfConfig.tag(), err.message, route);
        if (res._headerSent) {
            return false;
        }

        res.send(new restify.InternalError("unexpected error"));
        return true;
    });

    if (hfConfig.usesHttps()) {
        https_server.on('uncaughtException', function(req, res, route, err) {
            logger.bunyanLogger().error("%sUncaught Exception: %s on route %s", hfConfig.tag(), err.message, route);
            if (res._headerSent) {
                return false;
            }

            res.send(new restify.InternalError("unexpected error"));
            return true;
        });
    }

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
    var isStartupConnectionAttempt = true;
    var connectWithRetry = function() {
        logger.bunyanLogger().info('%sAttempting to connect to MongoDB. isStartupConnectionAttempt = %s', hfConfig.tag(), isStartupConnectionAttempt);
        var connectionString = hfConfig.connectionString();
        return hfConfig.dbUtils().connectToMongoDB(connectionString, function(err) {
            if (err && isStartupConnectionAttempt) {
                // Re-attempt only if this is part of app startup. Otherwise, we rely on auto-reconnect in
                // the mongodb native driver to try again.
                logger.bunyanLogger().error('%sFailed to connect to MongoDB. Attempting to connect again. Err: %s', hfConfig.tag(), err.message);
                setTimeout(connectWithRetry, 5000);
                return;
            }
            else {
                isStartupConnectionAttempt = false;
                logger.bunyanLogger().info('%sSuccessfully connected to MongoDB', hfConfig.tag());
                if (dbconnected_callback) {
                    dbconnected_callback();
                }
            }
        });
    };

    logger.bunyanLogger().info("%s*** Calling connectWithRetry", hfConfig.tag());
    connectWithRetry();

    //
    // Start the servers on the appropriate ports
    //
    server.listen(process.env.PORT || 1338 , function() {
        logger.bunyanLogger().info('%s%s listening at %s', hfConfig.tag(), server.name, server.url);

        if (hfConfig.usesHttps()) {
            https_server.listen(443, function() {
                logger.bunyanLogger().info('%s%s listening at %s', hfConfig.tag(), https_server.name, https_server.url);

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
};
