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

UserLocationSchema.statics.collectionName = COLLECTION_NAME;

UserLocationSchema.statics.createUserLocation = function(userId, location, cb) {
    var UserLocation = mongoose.model('UserLocation', UserLocationSchema);
    var userLoc = new UserLocation({userId: userId, location: location});
    userLoc.save(function(err, userLoc) {
        if (cb) {
            if (err) {
                return cb(null, err);
            }
            else {
                return cb(userLoc);
            }
        }
    });
};

module.exports = mongoose.model('UserLocation', UserLocationSchema);
