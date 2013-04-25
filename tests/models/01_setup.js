var should = require('should'),
    DBUtils = require('../../models/dbutils');

var usingMongoLab = false;

var connectionString = 'mongodb://localhost:27017/unit-tests';
if (usingMongoLab) {
    connectionString = 'mongodb://hfmongo:D0ntBl1nk@ds053497.mongolab.com:53497/unit-tests';
}

before(function(done) {
    DBUtils.connectToMongoDB(connectionString, {server:{poolSize:10,auto_reconnect:true}}, done);
});

after(function(done) {
    if (usingMongoLab) return done();

    DBUtils.deleteMongoDB(done);
});
