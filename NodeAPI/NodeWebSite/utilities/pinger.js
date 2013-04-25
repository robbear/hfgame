var logger = require('../logger/logger'),
    utilities = require('./utilities'),
    hfConfig = require('../config/config.js');

var User = hfConfig.userModel();

function ping() {
    logger.bunyanLogger().info("%s ***** PING *****");
    User.count({}, function(err, count) {
        logger.bunyanLogger().info("%s ***** PING RESULT: %s *****", hfConfig.tag(), err ? err.message : "ok");

        if (hfConfig.usePinger()) {
            setTimeout(ping, hfConfig.pingerTimeoutSeconds() * 1000);
        }
    });
}

exports.start = function() {
    if (!hfConfig.usePinger()) {
        return;
    }

    setTimeout(ping, hfConfig.pingerTimeoutSeconds() * 1000);
};