var should = require('should'),
    User = require('../../models/user');

describe('User', function() {
    var MAX_USERS = 10;

    describe('#createUser()', function() {
        var funcs = [];

        function createFunc(i, done) {
            return function(userName, password) {
                User.createUser('user'+(i+1)+'@test.com', 'user'+(i+1)+'password', function(err, user) {
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

    describe('#createUser() - try existing username', function() {
        it('should return an error indicating that the username already exists', function(done) {
            User.createUser('user1@test.com', 'user1password', function(err, user) {
                should.exist(err);
                err.code.should.equal(11000);
                should.not.exist(user);
                done();
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

    describe('Authentication: non-existent user', function() {
        var userName = 'foobar@foobar.com';
        var userPassword = 'foobarfoobar';

        it('should reject a non-existent user login attempt', function(done) {
            User.getAuthenticated(userName, userPassword, function(err, user, reason) {
                should.not.exist(err);
                should.exist(reason);
                should.not.exist(user);
                reason.should.equal(User.failedLogin.NOT_FOUND);
                done();
            });
        });
    });

    describe('Verify account lockout with bad password retries', function() {
        var RETRY_ATTEMPTS = User.maxLoginAttempts;
        var funcs = [];
        var userName = 'user1@test.com';
        var password = 'boguspassword';

        before(function(done) {
            function createFunc(i) {
                return function(userName, password) {
                    User.getAuthenticated(userName, password, function(err, user, reason) {
                        should.not.exist(err);
                        should.exist(reason);
                        should.not.exist(user);
                        reason.should.equal(User.failedLogin.PASSWORD_INCORRECT);
                        i++;
                        if (i < RETRY_ATTEMPTS) {
                            funcs[i](userName, password);
                        }
                        else {
                            done();
                        }
                    });
                }
            }

            for (var i = 0; i < RETRY_ATTEMPTS; i++) {
                funcs[i] = createFunc(i);
            }

            funcs[0](userName, password);
        });

        it('should fail authentication based on account lock', function(done) {
            User.getAuthenticated(userName, 'user1password', function(err, user, reason) {
                should.not.exist(err);
                should.exist(reason);
                should.not.exist(user);
                reason.should.equal(User.failedLogin.MAX_ATTEMPTS);
                if (err) {
                    done(err);
                }
                else {
                    done();
                }
            });
        });
    });

    describe('Verify account lockout times out and allows login (note delay of ' + (User.accountLockoutMilliseconds / 1000) + ' seconds)', function() {
        before(function(done) {
            this.timeout(User.accountLockoutMilliseconds + 5000);
            setTimeout(done, User.accountLockoutMilliseconds);
        });

        it('should unlock the account and allow authentication', function(done) {
            User.getAuthenticated('user1@test.com', 'user1password', function(err, user, reason) {
                should.not.exist(err);
                should.not.exist(reason);
                should.exist(user);
                user.should.have.property('username', 'user1@test.com');
                if (err) {
                    done(err);
                }
                else {
                    done();
                }
            });
        });
    });

});
