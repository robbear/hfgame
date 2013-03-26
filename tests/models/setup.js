var should = require('should'),
    TestUtils = require('../../models/testutils');

var connectionString = 'mongodb://localhost:27017/user-test';

before(function(done) {
    TestUtils.connectToMongoDB(connectionString, function(err) {
        if (err) return done(err);
        done();
    });
});

after(function(done) {
    TestUtils.disconnectFromMongoDB(function(err) {
        if (err) {
            done(err);
        }
        else {
            done();
        }
    });
});
