var should = require('should'),
    server = require('../../NodeAPI/NodeWebSite/start_server.js');

before(function(done) {
    this.timeout(10000);
    server.SilenceLogger(true);
    server.StartServer(done);
});

after(function(done) {
    done();
});
