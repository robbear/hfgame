var mongoose = require('mongoose');

var Schema = mongoose.Schema,
    COLLECTION_NAME = 'userlocation';

var safe = true;

var UserLocationSchema = new Schema({
    userId: { type: Schema.Types.ObjectId, required: true, index: 1 },
    location: { type: { type: String }, coordinates: [] },
    altitude: { type: Number, default: 0.0 },
    accuracy: { type: Number, default: 0.0 },
    date: { type: Date, default: Date.now() }
    }, {
    //autoIndex: false,
    safe: safe,
    collection: COLLECTION_NAME
});
// Turn off versioning since this collection is intended to be read-only
UserLocationSchema.set('versionKey', false);
UserLocationSchema.index({ location: '2dsphere' });

UserLocationSchema.statics.collectionName = COLLECTION_NAME;

UserLocationSchema.statics.createUserLocation = function(userId, location, altitude, accuracy, date, cb) {
    var UserLocation = mongoose.model('UserLocation', UserLocationSchema);
    var userLoc = new UserLocation({
        userId: userId,
        location: { type: 'Point', coordinates: location },
        altitude: altitude,
        accuracy: accuracy,
        date: date ? date : Date.now()
    });
    userLoc.save(function(err, userLoc) {
        if (cb) {
            return cb(err, userLoc);
        }
    });
};

UserLocationSchema.statics.insertUserLocations = function(userId, locationsWithDate, cb) {
    // Note: We expect the locations array to be validated. Calling code, particularly
    // from REST interfaces, should validate/protect before calling.
    if (!Array.isArray(locationsWithDate)) {
        process.nextTick(cb);
        return;
    }

    //
    // locationsWithDate:
    // [{ coordinates: [lon, lat], altitude: altitude, accuracy: accuracy, date: Date }]
    //

    var UserLocation = mongoose.model('UserLocation', UserLocationSchema);
    var modelLocs = new Array(locationsWithDate.length);
    for (var i = 0; i < locationsWithDate.length; i++) {
        var modelItem = new UserLocation({
            userId: userId,
            location: { type: 'Point', coordinates: locationsWithDate[i].coordinates },
            altitude: locationsWithDate[i].altitude,
            accuracy: locationsWithDate[i].accuracy,
            date: locationsWithDate[i].date
        });
        modelLocs[i] = modelItem.toObject();
    }

    UserLocation.collection.insert(modelLocs, {}, function(err, docs) {
        if (cb) {
            return cb(err, docs);
        }
    });
};

UserLocationSchema.statics.getUserLocationsByDateRange = function(userId, start, end, limit, cb) {
    var UserLocation = mongoose.model('UserLocation', UserLocationSchema);

    var query = UserLocation.find({});
    query.where('userId', userId);
    query.where('date').gte(start);
    query.where('date').lt(end);
    if (limit) {
        query.limit(limit);
    }

    query.exec(function(err, docs) {
        if (cb) {
            return cb(err, docs);
        }
    });
};

//
// This static method may not be generally useful, but we've implemented it (early in this work)
// for testing purposes and for code demonstration purposes. Note the query.where('location')
// clause and how we indicate to MongoDB how to find locations within a maxMeters radius.
// This is based on GeoJSON and spherical indexing.
//
UserLocationSchema.statics.getUserLocationsNear = function(location, maxMeters, limit, cb) {
    if (!Array.isArray(location) || location.length != 2) {
        throw "Invalid location array";
    }

    var UserLocation = mongoose.model('UserLocation', UserLocationSchema);

    var query = UserLocation.find({});
    query.where('location').near({ type: 'Point', coordinates: location }).maxDistance(maxMeters);
    if (limit) {
        query.limit(limit);
    }

    query.exec(function(err, docs) {
        if (cb) {
            return cb(err, docs);
        }
    });
};

module.exports = mongoose.model('UserLocation', UserLocationSchema);
