var restify = require('restify'),
    should = require('should');

var client = restify.createJsonClient({
    version: '*',
    url: "http://localhost:1338"
});

var username = 'restuser@test.com';
var password = 'password';

describe('Users REST API', function() {

    describe('/users/createuser', function() {
        it('should create a test user with { username: \'' + username + '\', password: \'' + password + '\' }', function(done) {
            client.post('/users/createuser', { username: username, password: password }, function(err, req, res, user) {
                should.not.exist(err);
                should.exist(user);
                if (err || !user) {
                    return done(err);
                }

                res.statusCode.should.equal(200);
                user.should.have.property('username');
                user.should.have.property('id');
                user.username.should.equal(username);
                user.id.should.be.a('string');
                return done();
            });
        });
    });

    describe('/users/login', function() {
        it('should allow a login for user { username: \'' + username + '\', password: \'' + password + '\' }', function(done) {
            var url = '/users/login?username=' + username + '&password=' + password;
            client.get(url, function(err, req, res, user) {
                should.not.exist(err);
                should.exist(user);
                if (err || !user) {
                    return done(err);
                }

                res.statusCode.should.equal(200);
                user.should.have.property('username');
                user.should.have.property('id');
                user.username.should.equal(username);
                user.id.should.be.a('string');
                return done();
            });
        });
    });
});