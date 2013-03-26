var should = require('should'),
    User = require('../../models/user'),
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

describe('User', function() {
    var MAX_USERS = 10;

    describe('#createUser()', function() {
        var funcs = [];

        function createFunc(i, done) {
            return function(userName, password) {
                User.createUser('user'+(i+1)+'@test.com', 'user'+(i+1)+'password', function(user, err) {
                    should.not.exist(err);
                    user.should.have.property('username', 'user'+(i+1)+'@test.com');
                    if (err) {
                        done(err);
                    }
                    i++;
                    if (i < MAX_USERS) {
                        funcs[i]();
                    }
                    else {
                        done();
                    }
                });
            };
        }

        it('should create ' + MAX_USERS + ' users of the form userN@test.com', function(done) {
            for (var i = 0; i < MAX_USERS; i++) {
                funcs[i] = createFunc(i, done);
            }

            funcs[0]();
        });
    });

    describe('Verify MAX_USERS via User.find', function() {
        it('should find ' + MAX_USERS + ' users in the database', function(done) {
            User.find(function(err, users) {
                should.not.exist(err);
                if (err) {
                    done(err);
                }
                else {
                    users.should.have.lengthOf(MAX_USERS);
                    done();
                }
            });
        });
    });

    describe('Verify MAX_USERS by count', function() {
        it('should find ' + MAX_USERS + ' users in the database via the User.count method', function(done) {
            User.count({}, function(err, count) {
                should.not.exist(err);
                if (err) {
                    done(err);
                }
                else {
                    count.should.equal(MAX_USERS);
                    done();
                }
            });
        });
    });

    describe('#findOne', function() {
        it('should find one user with username==\'user1@test.com\'', function(done) {
            User.findOne({username: 'user1@test.com'}, function(err, user) {
                should.not.exist(err);
                if (err) {
                    done(err);
                }
                else {
                    user.should.have.property('username', 'user1@test.com');
                    done();
                }
            });
        });
    });

    describe('#getAuthenticated()', function() {
        var userName = 'user1@test.com';
        var userPassword = 'user1password';

        it('should authenticate user1@test.com', function(done) {
            User.getAuthenticated(userName, userPassword, function(err, user, reason) {
                should.not.exist(err);
                should.not.exist(reason);
                if (err) {
                    done(err);
                }
                should.exist(user);
                user.should.have.property('username', userName);
                done();
            });
        });
    });

    describe('Authentication: bad password', function() {
        var userName = 'user1@test.com';
        var userPassword = 'foobarfoobar';

        it('should reject a bad password attempt', function(done) {
            User.getAuthenticated(userName, userPassword, function(err, user, reason) {
                should.not.exist(err);
                should.exist(reason);
                should.not.exist(user);
                reason.should.equal(User.failedLogin.PASSWORD_INCORRECT);
                done();
            });
        });
    });
});
