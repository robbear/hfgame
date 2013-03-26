var mongoose = require('mongoose');

var testUtils = {
    database: {},

    connectToMongoDB: function(connectionString, cb) {
        mongoose.connect(connectionString);
        this.database = mongoose.connection;

        this.database.on('open', function() {
            if (cb) return cb();
        });

        this.database.on('error', function(err) {
            if (cb) return cb(err);
        });
    },

    disconnectFromMongoDB: function(cb) {
        this.database.db.dropDatabase(function(err) {
            if (cb) {
                if (err) {
                    return cb(err);
                }
                else {
                    return cb();
                }
            }
        });
    }
};

module.exports = testUtils;