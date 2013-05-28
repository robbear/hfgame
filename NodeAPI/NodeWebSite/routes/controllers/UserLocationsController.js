var restify = require('restify'),
    fs = require('fs'),
    logger = require('../../logger/logger'),
    utilities = require('../../utilities/utilities'),
    hfConfig = require('../../config/config.js');

var UserLocation = hfConfig.userLocationModel;

exports.createUserLocation = function(req, res, next) {
    var userId = req.params.userId;
    var coordinates = req.params.coordinates;
    var date = req.params.date;
    var altitude = req.params.altitude;
    var accuracy = req.params.accuracy;

    logger.bunyanLogger().info(
        "%sREST API userlocations/createlocation: userId=%s, coordinates=%s, date=%s",
        hfConfig.TAG, userId, coordinates, date);

    //
    // Validate the data
    //
    if (!userId) {
        return next(new restify.InvalidArgumentError("No userId"));
    }
    if (!isValidDate(date)) {
        return next(new restify.RestError("Invalid date"));
    }
    if (!isValidCoordinates(coordinates)) {
        return next(new restify.InvalidArgumentError("Invalid coordinates"));
    }
    if (!altitude || !isNumber(accuracy)) {
        altitude = 0.0;
    }
    if (!accuracy || !isNumber(accuracy)) {
        accuracy = 0.0;
    }

    UserLocation.createUserLocation(userId, coordinates, altitude, accuracy, date, function(err, userLocation) {
        if (err) {
            logger.bunyanLogger().error("%s... userlocations/createlocation: err=%s", hfConfig.TAG, err.message);
            if (utilities.isErrorDatabaseDisconnect(err)) {
                return next(new restify.InternalError("unexpected database error"));
            }
            else {
                return next(new restify.RestError(err.message));
            }
        }

        if (!userLocation) {
            logger.bunyanLogger().info("%s... userlocations/createlocation: No userLocation found for %s", hfConfig.TAG, userId);
            return next(new restify.RestError("Unable to add location for userId: " + userId));
        }

        logger.bunyanLogger().info("%s... userlocations/createlocation: Successfully created location for user %s", hfConfig.TAG, userId);
        res.send({});
        next();
    });
};

exports.insertUserLocations = function(req, res, next) {
    var userId = req.params.userId;
    var userLocations = req.params.locations;

    var count = userLocations ? (userLocations.length ? userLocations.length : 0) : 0;
    logger.bunyanLogger().info("%sREST API userlocations/createlocations: userId=%s, count=%d", hfConfig.TAG, userId, count);

    //
    // Validate the data
    //
    if (!userId) {
        return next(new restify.InvalidArgumentError("No userId"));
    }
    if (!userLocations) {
        return next(new restify.InvalidArgumentError("No locations provided"));
    }
    if (!Array.isArray(userLocations)) {
        return next(new restify.InvalidArgumentError("locations must be an array of objects: { location: [lon, lat], altitude: altitude, accuracy: accuracy, date: date }"));
    }
    if (userLocations.length < 1) {
        return next(new restify.InvalidArgumentError("locations must be an array of objects: { location: [lon, lat], altitude: altitude, accuracy: accuracy, date: date }"));
    }
    for (var i = 0; i < userLocations.length; i++) {
        var ul = userLocations[i];
        var coordinates = ul.coordinates;
        var altitude = ul.altitude;
        var accuracy = ul.accuracy;
        if (!isValidCoordinates(coordinates)) {
            return next(new restify.InvalidArgumentError("One or more invalid location coordinates"));
        }
        if (!isValidDate(ul.date)) {
            return next(new restify.InvalidArgumentError("One or more invalid dates}"));
        }
        if (!altitude || !isNumber(accuracy)) {
            userLocations[i].altitude = 0.0;
        }
        if (!accuracy || !isNumber(accuracy)) {
            userLocations[i].accuracy = 0.0;
        }
    }

    UserLocation.insertUserLocations(userId, userLocations, function(err, docs) {
        if (err) {
            logger.bunyanLogger().error("%s... userlocations/createlocations error: %s", hfConfig.TAG, err.message);
            if (utilities.isErrorDatabaseDisconnect(err)) {
                return next(new restify.InternalError("unexpected database error"));
            }
            else {
                return next(new restify.RestError(err.message));
            }
        }

        if (!docs) {
            logger.bunyanLogger().info("%s... userlocations/createlocations - Unable to add locations for userId: %s", hfConfig.TAG, userId);
            return next(new restify.RestError("Unable to add locations for userId: " + userId));
        }

        logger.bunyanLogger().info("%s... userlocations/createLocations success", hfConfig.TAG);
        res.send({});
        next();
    });
};

exports.getUserLocationsByDate = function(req, res, next) {
    var userId = req.params.userId;
    var limit = req.params.limit;
    var startDate = req.params.start;
    var endDate = req.params.end;

    var MAX_LIMIT = 1000000;

    logger.bunyanLogger().info("%sREST API userlocations/bydate: userId=%s, limit=%d, start=%s, end=%s",
        hfConfig.TAG, userId, limit, startDate, endDate);

    //
    // Validate the data
    //
    if (!userId) {
        return next(new restify.InvalidArgumentError("No userId"));
    }
    if (!isValidDate(startDate)) {
        return next(new restify.InvalidArgumentError("Invalid start date"));
    }
    if (!isValidDate(endDate)) {
        return next(new restify.InvalidArgumentError("Invalid end date"));
    }
    if (!limit) {
        limit = MAX_LIMIT;
    }
    else {
        limit = parseInt(limit, 10);
    }
    if (!limit || (typeof limit !== 'number') || limit < 1) {
        limit = MAX_LIMIT;
    }

    UserLocation.getUserLocationsByDateRange(userId, startDate, endDate, limit, function(err, docs) {
        // BUGBUG
        // TODO: Need to document return data format
        if (err) {
            logger.bunyanLogger().error("%s... userlocations/bydate error: %s", hfConfig.TAG, err.message);
            if (utilities.isErrorDatabaseDisconnect(err)) {
                return next(new restify.InternalError("unexpected database error"));
            }
            else {
                return next(new restify.RestError(err.message));
            }
        }

        var locations = new Array(docs.length);
        for (var i = 0; i < docs.length; i++) {
            var doc = docs[i];
            locations[i] = { coordinates: doc.location.coordinates, altitude: doc.altitude, accuracy: doc.accuracy, date: doc.date };
        }

        logger.bunyanLogger().info("%s... userlocations/bydate success", hfConfig.TAG);
        res.send(locations);
        next();
    });
};

//
// BUGBUG
// TODO: Should these utility methods go in a shared module somewhere?
//
function isValidDate(obj) {
    var d;

    if (!obj) {
        return false;
    }

    try {
        d = new Date(obj);

        if (Object.prototype.toString.call(d) !== "[object Date]") {
            return false;
        }

        return !isNaN(d.getTime());
    }
    catch(err) {
        return false;
    }
}

function isValidCoordinates(coordinates) {
    if (!coordinates) {
        return false;
    }
    if (!Array.isArray(coordinates)) {
        return false;
    }
    if (coordinates.length != 2) {
        return false;
    }
    if ((typeof coordinates[0]) !== 'number' || (typeof coordinates[1]) !== 'number') {
        return false;
    }
    if (coordinates[0] > 180.0 || coordinates[0] < -180.0 || coordinates[1] > 90.0 || coordinates[1] < -90.0) {
        return false;
    }

    return true;
}

function isNumber(val) {
    return !isNaN(parseFloat(val) && isFinite(val));
}
