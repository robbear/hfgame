// TODO: This path needs to be set to the production models path, based on the build process. TBD.
var _modelsPath = '../../../models/';
// TODO: Set to true for production as part of the build process. TBD.
var _usingMongoLab = false;

var _databaseName = 'hfgame';
var _usesHttps = false;
var _userModel = require(_modelsPath + 'user.js');
var _dbUtils = require(_modelsPath + 'dbutils.js');
var _userlocationModel = require(_modelsPath + 'userlocation.js');

exports.usesHttps = function () {
    return _usesHttps;
};

exports.connectionString = function() {
    var connectionString = 'mongodb://localhost:27017/' + _databaseName;
    if (_usingMongoLab) {
        connectionString = 'mongodb://hfmongo:D0ntBl1nk@ds053497.mongolab.com:53497/' + _databaseName;
    }

    return connectionString;
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

//
// Primarily used for unit tests to set the database name
//
exports.setDatabaseName = function(dbName) {
    _databaseName = dbName;
};
