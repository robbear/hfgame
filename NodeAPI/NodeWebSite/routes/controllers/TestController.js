var restify = require('restify'),
    fs = require('fs'),
    logger = require('../../logger/logger'),
    utilities = require('../../utilities/utilities'),
    hfConfig = require('../../config/config.js');

var User = hfConfig.userModel;

exports.testRoute = function(req, res, next) {
    logger.bunyanLogger().info("%sREST API /test", hfConfig.TAG);

    var isDatabaseOk = true;

    User.count({}, function(err, count) {
        if (err) {
            logger.bunyanLogger().error("%s... /test failed: err=%s", hfConfig.TAG, err.message);
            if (utilities.isErrorDatabaseDisconnect(err)) {
                isDatabaseOk = false;
            }
        }

        logger.bunyanLogger().info("%s... /test: %s", hfConfig.TAG, isDatabaseOk ? "Database is reachable" : "Database is not reachable");
        res.send({ REST: "ok", Database: isDatabaseOk ? "ok" : "error", Date: (new Date()).toString() });
        next();
    });
};
