var mongoose = require('mongoose');

var Schema = mongoose.Schema,
    COLLECTION_NAME = 'userlocation';

var UserLocationSchema = new Schema({
    userId: { type: Schema.Types.ObjectId, required: true },
    location: { lon: Number, lat: Number },
    creationDate: { type: Date, default: Date.now }
}, {
    collection: COLLECTION_NAME
}, {
//    autoIndex: false
});
UserLocationSchema.index({ loc: '2d' });
UserLocationSchema.index({ userId: 1 });
// Turn off versioning since this collection is intended to be read-only
UserLocationSchema.set('versionKey', false);

UserLocationSchema.statics.collectionName = COLLECTION_NAME;

UserLocationSchema.statics.createUserLocation = function(userId, location, cb) {
    var UserLocation = mongoose.model('UserLocation', UserLocationSchema);
    var userLoc = new UserLocation({userId: userId, location: location});
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

    var UserLocation = mongoose.model('UserLocation', UserLocationSchema);
    var modelLocs = new Array(locationsWithDate.length);
    for (var i = 0; i < locationsWithDate.length; i++) {
        var modelItem = new UserLocation({userId: userId, location: locationsWithDate[i].location, creationDate: locationsWithDate[i].creationDate});
        modelLocs[i] = modelItem.toObject();
    }

    UserLocation.collection.insert(modelLocs, {}, function(err, docs) {
        if (cb) {
            return cb(err, docs);
        }
    });
}

module.exports = mongoose.model('UserLocation', UserLocationSchema);
