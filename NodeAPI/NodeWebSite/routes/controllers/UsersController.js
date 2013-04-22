var restify = require('restify'),
    fs = require('fs'),
    logger = require('../../logger/logger'),
    hfConfig = require('../../config/config.js');

var User = hfConfig.userModel();

exports.createUser = function(req, res, next) {
    var userName = req.params.username;
    var password = req.params.password;

    logger.bunyanLogger().info("%sREST API users/createuser: userName=%s", hfConfig.tag(), userName);

    if (!userName || !password) {
        logger.bunyanLogger().info("%s... users/createuser: null userName or null password", hfConfig.tag());
        return next(new restify.InvalidArgumentError("Invalid username or password"));
    }

    User.createUser(userName, password, function(err, user) {
        if (err || !user) {
            if (err) {
                logger.bunyanLogger().error("%s... users/createuser: err=%s", hfConfig.tag(), err.message);
            }
            logger.bunyanLogger().info("%s... users/createuser: Invalid username or password", hfConfig.tag());
            return next(new restify.InvalidArgumentError("Invalid username or password"));
        }
        else {
            //
            // BUGBUG
            // TODO: Need to define client model infrastructure.
            //
            logger.bunyanLogger().info("%s... users/createuser: success. userid=%s, username=%s", hfConfig.tag(), user.id, user.username);
            res.send({ id: user.id, username: user.username });
            next();
        }
    });
};

exports.login = function(req, res, next) {
    var userName = req.params.username;
    var password = req.params.password;

    logger.bunyanLogger().info("%sRESTAPI users/login: userName=%s", hfConfig.tag(), userName);

    if (!userName || !password) {
        logger.bunyanLogger().info("%s... users/login: null userName or null password", hfConfig.tag());
        return next(new restify.InvalidArgumentError("Invalid username or password"));
    }

    User.getAuthenticated(userName, password, function(err, user, reason) {
        if (err || !user) {
            if (err) {
                logger.bunyanLogger().error("%s... users/login failed: err=%s", hfConfig.tag(), err.message);
            }
            logger.bunyanLogger().info('%s... users/login: Failed to login user %s: %d', hfConfig.tag(), userName, reason);
            return next(new restify.InvalidArgumentError("Invalid username or password"));
        }

        //
        // BUGBUG
        // TODO: Need to define client model infrastructure
        //
        logger.bunyanLogger().info("%s... users/login: success. userid=%s, username=%s", hfConfig.tag(), user.id, user.username);
        res.send({ id: user.id, username: user.username });
        next();
    });
};