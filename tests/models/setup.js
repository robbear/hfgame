var should = require('should'),
    TestUtils = require('../../models/testutils');

var connectionString = 'mongodb://localhost:27017/user-test';

before(function(done) {
    TestUtils.connectToMongoDB(connectionString, done);
});

after(function(done) {
    TestUtils.disconnectFromMongoDB(done);
});
