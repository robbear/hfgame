// TODO: This path needs to be set to the production models path, based on the build process. TBD.
var _modelsPath = '../../../models/';
// TODO: Set to true for production as part of the build process. TBD.
var _usingMongoLab = false;

var _projectName = 'hfgame';
var _usesHttps = false;
var _userModel = require(_modelsPath + 'user.js');
var _dbUtils = require(_modelsPath + 'dbutils.js');
var _userlocationModel = require(_modelsPath + 'userlocation.js');

var _connectionString = 'mongodb://localhost:27017/' + _projectName;
if (_usingMongoLab) {
    _connectionString = 'mongodb://hfmongo:D0ntBl1nk@ds053497.mongolab.com:53497/' + _projectName;
}

exports.usesHttps = function () {
    return _usesHttps;
};

exports.connectionString = function() {
    return _connectionString;
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
