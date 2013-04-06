var restify = require('restify'),
    should = require('should');

var client = restify.createJsonClient({
    version: '*',
    url: "http://localhost:1338"
});

describe('Users REST API', function() {

    describe('#Foo()', function() {
        it('should eventually test something', function(done) {
            client.get('/api', function(err, req, res, data) {
                if (err) {
                    return done(err);
                }
                else {
                    res.statusCode.should.equal(200);
                    should.exist(data);
                    return done();
                }
            });
        });
    });
});