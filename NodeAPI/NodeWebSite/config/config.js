var logger = require('../logger/logger');

var _env = process.env.NODE_ENV || 'development';

//
// Configuration
//
var _modelsPath;
var _mongoBaseUrl;
var _databaseName = 'hfgame';
var _usesHttps = false;
var _useRestifyLogging = false;
var _TAG = "HFAPI: ";
var _pingerTimeoutSeconds = 45;
var _usePinger = true;

if ('development' === _env) {
    _modelsPath = '../../../models/';
    _mongoBaseUrl = 'mongodb://localhost:27017/';
}
if ('production' === _env) {
    _modelsPath = '../models/';
    _mongoBaseUrl = 'mongodb://hfmongo:D0ntBl1nk@ds053497.mongolab.com:53497/';
}

//
// See https://groups.google.com/forum/?fromgroups=#!topic/mongoose-orm/0bOPcbCD12Q for
// information regarding keep-alive
//
exports.databaseOptions = {
    /*
    replset: {
        strategy: 'ping',
        rs_name: 'somerepsetname',
        readSecondary: true,
        socketOptions: {
            keepAlive: 1
        }
    },
    */
    server: {
        poolSize: 10,
        auto_reconnect: true,
        socketOptions: {
            keepAlive: 1
        }
    }
};

exports.usesHttps = _usesHttps;
exports.connectionString = _mongoBaseUrl + _databaseName;
exports.dbUtils = require(_modelsPath + 'dbutils.js');
exports.userModel = require(_modelsPath + 'user.js');
exports.userLocationModel = require(_modelsPath + 'userlocation.js');
exports.useRestifyLogging = _useRestifyLogging;
exports.TAG = _TAG;
exports.usePinger = _usePinger;
exports.pingerTimeoutSeconds = _pingerTimeoutSeconds;
exports.environment = _env;

exports.useLogging = function(useLogging) {
    logger.useLogging(useLogging);
};

exports.setRestifyLogging = function(useRestifyLogging) {
    _useRestifyLogging = useRestifyLogging;
};

exports.setPinger = function(usePinger, seconds) {
    _usePinger = usePinger;
    _pingerTimeoutSeconds = seconds;
};

//
// Primarily used for unit tests to set the database name
//
exports.setDatabaseName = function(dbName) {
    _databaseName = dbName;
};
