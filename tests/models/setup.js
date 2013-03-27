var should = require('should'),
    TestUtils = require('../../models/testutils');

var usingMongoLab = false;

var connectionString = 'mongodb://localhost:27017/unit-tests';
if (usingMongoLab) {
    connectionString = 'mongodb://hfmongo:D0ntBl1nk@ds053497.mongolab.com:53497/unit-tests';
}

before(function(done) {
    TestUtils.connectToMongoDB(connectionString, done);
});

after(function(done) {
    if (usingMongoLab) return done();

    TestUtils.deleteMongoDB(done);
});
