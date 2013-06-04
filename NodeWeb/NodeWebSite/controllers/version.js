
var express = require('express'),
    connect = require('connect'),
    utilities = require('../utilities/utilities');

var app = module.exports = express();

app.get('/version', function(req, res) {
    var appVersion = (utilities.versionString === "staticfiles") ? "Unspecified" : utilities.versionString;
    utilities.sendResponse(res, {
        app: appVersion,
        buildstring: utilities.timestampString,
        nodejs: process.version,
        connect: connect.version,
        express: express.version
    });
});