var restify = require('restify'),
    fs = require('fs'),
    logger = require('../../logger/logger'),
    hfConfig = require('../../config/config.js');

var UserLocation = hfConfig.userLocationModel();

exports.createUserLocation = function(req, res, next) {
    var userId = req.params.userId;
    var coords = req.params.coordinates;
    var date = req.params.date;

    if (!userId || !coords || !Array.isArray(coords) || coords.length != 2) {
        return next(new restify.InvalidArgumentError("One or more invalid arguments"));
    }

    UserLocation.createUserLocation(userId, coords, date, function(err, userLocation) {
        if (err) {
            return next(new restify.RestError(err.message));
        }

        if (!userLocation) {
            return next(new restify.RestError("Unable to add location for userId: " + userId));
        }

        res.send({ userId: userLocation.userId, coordinates: userLocation.location.coordinates, date: userLocation.date });
        next();
    });
};

exports.insertUserLocations = function(req, res, next) {
    var userId = req.params.userId;
    var userLocations = req.params.locations;

    //
    // Validate the data
    // BUGBUG
    // TODO: Consider whether this data validation should go in the model class method
    //
    if (!userId) {
        return next(new restify.InvalidArgumentError("No userid"));
    }
    if (!userLocations) {
        return next(new restify.InvalidArgumentError("No userlocations"));
    }
    if (!Array.isArray(userLocations)) {
        return next(new restify.InvalidArgumentError("userlocations must be an array of objects: { location: [lon, lat], date: date }"));
    }
    if (userLocations.length < 1) {
        return next(new restify.InvalidArgumentError("userlocations must be an array of objects: { location: [lon, lat], date: date }"));
    }
    for (var i = 0; i < userLocations.length; i++) {
        var ul = userLocations[i];
        var coordinates = ul.coordinates;
        if (!coordinates ||
            !Array.isArray(coordinates) ||
            coordinates.length != 2 ||
            (typeof coordinates[0]) !== 'number' ||
            (typeof coordinates[1]) !== 'number') {
            return next(new restify.InvalidArgumentError("userlocations must be an array of objects: { location: [lon, lat], date: date }"));
        }
        // BUGBUG
        // TODO: Can we pass in time ticks instead of string?
        if (!ul.date || (typeof ul.date) !== 'string') {
            return next(new restify.InvalidArgumentError("userlocations must be an array of objects: { location: [lon, lat], date: date }"))
        }

        // BUGBUG
        // TODO: Validate longitude and latitude values
    }

    UserLocation.insertUserLocations(userId, userLocations, function(err, docs) {
        if (err) {
            return next(new restify.RestError(err.message));
        }

        if (!docs) {
            return next(new restify.RestError("Unable to add locations for userId: " + userId));
        }

        res.send({count: docs.length});
        next();
    });
};