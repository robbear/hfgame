var restify = require('restify'),
    fs = require('fs'),
    logger = require('../../logger/logger'),
    hfConfig = require('../../config/config.js');

var User = hfConfig.userModel();

exports.createUser = function(req, res, next) {
    User.createUser('user1@test.com', 'user1password', function(err, user) {
        // BUGBUG - process error for API reporting
        res.send(err ? err : user);
        next();
    });
};