var restify = require('restify'),
    fs = require('fs'),
    logger = require('../../logger/logger'),
    utilities = require('../../utilities/utilities'),
    hfConfig = require('../../config/config.js');

var User = hfConfig.userModel;

exports.createUser = function(req, res, next) {
    var userName = req.params.username;
    var password = req.params.password;

    logger.bunyanLogger().info("%sREST API users/createuser: userName=%s", hfConfig.TAG, userName);

    if (!userName || !password) {
        logger.bunyanLogger().info("%s... users/createuser: null userName or null password", hfConfig.TAG);
        return next(new restify.InvalidArgumentError("Invalid username or password"));
    }

    User.createUser(userName, password, function(err, user) {
        if (err || !user) {
            if (err) {
                logger.bunyanLogger().error("%s... users/createuser: err=%s", hfConfig.TAG, err.message);
                if (utilities.isErrorDatabaseDisconnect(err)) {
                    return next(new restify.InternalError("unexpected database error"));
                }
            }
            logger.bunyanLogger().info("%s... users/createuser: Invalid username or password", hfConfig.TAG);
            return next(new restify.InvalidArgumentError("Invalid username or password"));
        }
        else {
            //
            // BUGBUG
            // TODO: Need to define client model infrastructure.
            //
            logger.bunyanLogger().info("%s... users/createuser: success. userid=%s, username=%s", hfConfig.TAG, user.id, user.username);
            res.send({ id: user.id, username: user.username });
            next();
        }
    });
};

exports.login = function(req, res, next) {
    var userName = req.params.username;
    var password = req.params.password;

    logger.bunyanLogger().info("%sRESTAPI users/login: userName=%s", hfConfig.TAG, userName);

    if (!userName || !password) {
        logger.bunyanLogger().info("%s... users/login: null userName or null password", hfConfig.TAG);
        return next(new restify.InvalidArgumentError("Invalid username or password"));
    }

    User.getAuthenticated(userName, password, function(err, user, reason) {
        if (err || !user) {
            if (err) {
                logger.bunyanLogger().error("%s... users/login failed: err=%s", hfConfig.TAG, err.message);
                if (utilities.isErrorDatabaseDisconnect(err)) {
                    return next(new restify.InternalError("unexpected database error"));
                }
            }
            logger.bunyanLogger().info('%s... users/login: Failed to login user %s: %d', hfConfig.TAG, userName, reason);
            return next(new restify.InvalidArgumentError("Invalid username or password"));
        }

        //
        // BUGBUG
        // TODO: Need to define client model infrastructure
        //
        logger.bunyanLogger().info("%s... users/login: success. userid=%s, username=%s", hfConfig.TAG, user.id, user.username);
        res.send({ id: user.id, username: user.username });
        next();
    });
};