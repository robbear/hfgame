var _isClientLoggingEnabled = false;    // Set to false for production. If true, console.log is used.
var _logStaticResources = false;        // Set to false for production. If true, logs all HTTP requests

exports.isClientLoggingEnabled = function() {
    return _isClientLoggingEnabled;
}

exports.logStaticResources = function() {
    return _logStaticResources;
}
