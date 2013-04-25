var mongoose = require('mongoose'),
    connectionOpenCallback = null,
    connectionErrorCallback = null,
    debug = false;

if (debug) {
    mongoose.set('debug', true);
}

mongoose.connection.on("error", function(err) {
    if (connectionErrorCallback) {
        connectionErrorCallback(err);
    }
});

mongoose.connection.on("open", function() {
    if (connectionOpenCallback) {
        connectionOpenCallback();
    }
});

var dbUtils = {
    connectToMongoDB: function(connectionString, databaseOptions, cb, cbOpen, cbError) {
        connectionOpenCallback = cbOpen;
        connectionErrorCallback = cbError;

        mongoose.connect(connectionString, databaseOptions, function(err) {
            if (cb) {
                return cb(err);
            }
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

module.exports = dbUtils;