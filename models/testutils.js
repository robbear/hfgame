var mongoose = require('mongoose'),
    debug = false;

if (debug) {
    mongoose.set('debug', true);
}

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

    deleteMongoDB: function(cb) {
        // Example of native driver call
        mongoose.connection.db.executeDbCommand({dropDatabase:1}, function(err, result) {
            mongoose.connection.close();
            if (cb) {
                if (err) return cb(err);
                return cb();
            }
        });
    },

    dropCollection: function(collectionName, cb) {
        var collection = mongoose.connection.collections[collectionName];
        if (collection) {
            collection.drop(function(err) {
                if (cb) {
                    if (err && err.message != 'ns not found') {
                        return cb(err);
                    }
                    else {
                        return cb();
                    }
                }
            });
        }
    }
};

module.exports = testUtils;