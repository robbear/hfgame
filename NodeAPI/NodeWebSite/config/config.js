var logger = require('../logger/logger');
var _modelsPath = '../../../models/';
var _mongoBaseUrl = 'mongodb://localhost:27017/';

var _databaseName = 'hfgame';
var _usesHttps = false;
var _userModel = require(_modelsPath + 'user.js');
var _dbUtils = require(_modelsPath + 'dbutils.js');
var _userlocationModel = require(_modelsPath + 'userlocation.js');
var _useLogging = true;
var _useRestifyLogging = false;
var _TAG = "HFAPI: ";
var _usePinger = true;
var _pingerTimeoutSeconds = 15;

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

exports.usesHttps = function() {
    return _usesHttps;
};

exports.connectionString = function() {
    return _mongoBaseUrl + _databaseName;
};

exports.dbUtils = function() {
    return _dbUtils;
};

exports.userModel = function() {
    return _userModel;
};

exports.userLocationModel = function() {
    return _userlocationModel;
};

exports.useLogging = function(useLogging) {
    logger.useLogging(useLogging);
};

exports.setRestifyLogging = function(useRestifyLogging) {
    _useRestifyLogging = useRestifyLogging;
};

exports.getRestifyLogging = function() {
    return _useRestifyLogging;
};

exports.tag = function() {
    return _TAG;
};

exports.setPinger = function(usePinger, seconds) {
    _usePinger = usePinger;
    _pingerTimeoutSeconds = seconds;
};

exports.usePinger = function() {
    return _usePinger;
};

exports.pingerTimeoutSeconds = function() {
    return _pingerTimeoutSeconds;
};

//
// Primarily used for unit tests to set the database name
//
exports.setDatabaseName = function(dbName) {
    _databaseName = dbName;
};
