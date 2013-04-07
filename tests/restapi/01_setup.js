var should = require('should'),
    DBUtils = require('../../models/dbutils');
    server = require('../../NodeAPI/NodeWebSite/start_server.js');

before(function(done) {
    this.timeout(10000);
    server.SilenceLogger(true);
    server.SetDatabaseName("rest-tests");
    server.StartServer(done);
});

after(function(done) {
    DBUtils.deleteMongoDB(done);
});
