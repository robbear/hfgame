var restify = require('restify'),
    fs = require('fs'),
    logger = require('../../logger/logger'),
    hfConfig = require('../../config/config.js');

var User = hfConfig.userModel();

exports.createUser = function(req, res, next) {
    var userName = req.params.username;
    var password = req.params.password;

    if (!userName || !password) {
        return next(new restify.InvalidArgumentError("Invalid username or password"));
    }

    User.createUser(userName, password, function(err, user) {
        res.send(err ? err : user);
        next();
    });
};

exports.login = function(req, res, next) {
    var userName = req.params.username;
    var password = req.params.password;

    if (!userName || !password) {
        return next(new restify.InvalidArgumentError("Invalid username or password"));
    }

    User.getAuthenticated(userName, password, function(err, user, reason) {
        if (err || !user) {
            logger.bunyanLogger().error('Failed to login user ' + userName +  ': ' + reason);
            return next(new restify.InvalidArgumentError("Invalid username or password"));
        }

        res.send({result: "ok", user: {username: user.username, id: user.id}});
    });
};