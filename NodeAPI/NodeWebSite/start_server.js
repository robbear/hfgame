var _databaseName = null;

exports.SetDatabaseName = function(dbName) {
    _databaseName = dbName;
};

exports.StartServer = function(startserver_callback, dbconnected_callback) {
    var restify = require('restify'),
        fs = require('fs'),
        router = require('./routes/router'),
        logger = require('./logger/logger'),
        pinger = require('./utilities/pinger'),
        hfConfig = require('./config/config.js');

    logger.bunyanLogger().info("%s***** Starting hfapi web service *****", hfConfig.TAG);
    if (_databaseName) hfConfig.setDatabaseName(_databaseName);

    //
    // HTTP server options
    //
    var http_options = {
        name: "hfAPI HTTP",
        log: hfConfig.useRestifyLogging ? logger.bunyanLogger() : null
    }

    //
    // HTTPS server options
    //
    var https_options = hfConfig.usesHttps ? {
        name: "hfAPI HTTPS",
        key: fs.readFileSync('./certificates/ssl/self-signed/server.key'),
        certificate: fs.readFileSync('./certificates/ssl/self-signed/server.crt'),
        log: hfConfig.useRestifyLogging ? logger.bunyanLogger() : null
    } : null;


    // Instantiate the HTTP and HTTPS servers
    var server = restify.createServer(http_options);
    var https_server = hfConfig.usesHttps ? restify.createServer(https_options) : null;

    // Error handlers
    // BUGBUG - These should be updated to handle graceful shutdown of cluster
    // per http://nodejs.org/api/domain.html.
    server.on('uncaughtException', function(req, res, route, err) {
        logger.bunyanLogger().error("%s*****Uncaught Exception*****: %s on route %s", hfConfig.TAG, err.message, route);
        if (res._headerSent) {
            return false;
        }

        res.send(new restify.InternalError("unexpected error"));
        return true;
    });

    if (hfConfig.usesHttps) {
        https_server.on('uncaughtException', function(req, res, route, err) {
            logger.bunyanLogger().error("%s*****Uncaught Exception*****: %s on route %s", hfConfig.TAG, err.message, route);
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

    if (hfConfig.usesHttps) {
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
        if (hfConfig.usesHttps) {
            https_server[route.httpVerb](route.route, route.serverHandler);
        }
    }

    //
    // Connect to the database
    //

    var isStartupConnectionAttempt = true;

    function onDatabaseConnect(err) {
        if (err && isStartupConnectionAttempt) {
            // Re-attempt only if this is part of app startup. Otherwise, we rely on auto-reconnect in
            // the mongodb native driver to try again.
            logger.bunyanLogger().error('%sFailed to connect to MongoDB. Attempting to connect again. Err: %s', hfConfig.TAG, err.message);
            setTimeout(connectWithRetry, 5000);
            return;
        }
        else {
            isStartupConnectionAttempt = false;
            logger.bunyanLogger().info('%sMongoDB connection established', hfConfig.TAG);
            if (dbconnected_callback) {
                dbconnected_callback();
            }

            pinger.start();
        }
    }

    function onDatabaseReconnect() {
        logger.bunyanLogger().info('%sSuccessfully reconnected to MongoDB', hfConfig.TAG);
    }

    function onDatabaseDisconnect(err) {
        if (isStartupConnectionAttempt) return;

        logger.bunyanLogger().error('%sLost connection to MongoDB. Err: %s', hfConfig.TAG, err.message);
    }

    var connectWithRetry = function() {
        logger.bunyanLogger().info('%sAttempting to connect to MongoDB. isStartupConnectionAttempt = %s', hfConfig.TAG, isStartupConnectionAttempt);
        var connectionString = hfConfig.connectionString;
        return hfConfig.dbUtils.connectToMongoDB(connectionString, hfConfig.databaseOptions, onDatabaseConnect, null, onDatabaseDisconnect, onDatabaseReconnect);
    };

    logger.bunyanLogger().info("%s*** Calling connectWithRetry", hfConfig.TAG);
    connectWithRetry();

    //
    // Start the servers on the appropriate ports
    //
    server.listen(process.env.PORT || 1338 , function() {
        logger.bunyanLogger().info('%s%s listening at %s in %s mode', hfConfig.TAG, server.name, server.url, hfConfig.environment);
        logger.bunyanLogger().info("Using node.js %s", process.version);

        if (hfConfig.usesHttps) {
            https_server.listen(443, function() {
                logger.bunyanLogger().info('%s%s listening at %s', hfConfig.TAG, https_server.name, https_server.url);

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
