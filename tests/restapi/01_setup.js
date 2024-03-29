var should = require('should'),
    DBUtils = require('../../models/dbutils'),
    hfConfig = require('../../NodeAPI/NodeWebSite/config/config.js'),
    server = require('../../NodeAPI/NodeWebSite/start_server.js');

before(function(done) {
    this.timeout(10000);
    hfConfig.useLogging(false);
    hfConfig.setRestifyLogging(false);
    hfConfig.setPinger(false, 15);
    server.SetDatabaseName("rest-tests");
    server.StartServer(null, done);
});

after(function(done) {
    DBUtils.deleteMongoDB(done);
});
