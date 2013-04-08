var should = require('should'),
    User = require('../../models/user');
    UserLocation = require('../../models/userlocation');

describe('UserLocation Model', function() {
    var seattleLoc = [122.3331, 47.6097];
    var sanfranciscoLoc = [122.4183, 37.7750];
    var newyorkLoc = [74.0064, 40.7142];
    var bostonLoc = [71.0603, 42.3583];
    var bostonDate = new Date("2011-12-06T22:30:00Z");
    var newyorkDate = new Date("2011-05-06T19:00:00Z");
    var MAX_RANDOM_LOCATIONS = 1000;
    var START_DATE_STRING = "2013-01-01T12:00:00Z";
    var END_DATE_STRING = "2013-04-30T12:00:00Z";
    var userId;

    before(function(done) {
        User.createUser('userlocationtestuser@test.com', 'password', function(err, user) {
            if (err) {
                return done(err);
            }
            else {
                userId = user.id;
                done();
            }
        });
    });

    describe('#createUserLocation() - Seattle', function() {
        it('should create a location item for Seattle', function(done) {
            UserLocation.createUserLocation(userId, seattleLoc, null, function(err, userLocation) {
                should.not.exist(err);
                should.exist(userLocation);
                should.exist(userLocation.location);
                should.exist(userLocation.location.coordinates);
                should.exist(userLocation.userId);
                should.exist(userLocation.date);
                if (err) {
                    return done(err);
                }
                else {
                    userLocation.location.coordinates.should.have.lengthOf(2);
                    userLocation.location.coordinates[0].should.equal(seattleLoc[0]);
                    userLocation.location.coordinates[1].should.equal(seattleLoc[1]);
                    userLocation.userId.should.be.a('object');
                    userLocation.userId.toString().should.equal(userId);
                    userLocation.date.should.be.a('object');
                    done();
                }
            });
        });
    });

    describe('#createUserLocation() - San Francisco', function() {
        it('should create a location item for San Francisco', function(done) {
            UserLocation.createUserLocation(userId, sanfranciscoLoc, null, function(err, userLocation) {
                should.not.exist(err);
                should.exist(userLocation);
                should.exist(userLocation.location);
                should.exist(userLocation.location.coordinates);
                should.exist(userLocation.userId);
                if (err) {
                    return done(err);
                }
                else {
                    userLocation.location.coordinates[0].should.equal(sanfranciscoLoc[0]);
                    userLocation.location.coordinates[1].should.equal(sanfranciscoLoc[1]);
                    userLocation.userId.should.be.a('object');
                    userLocation.userId.toString().should.equal(userId);
                    done();
                }
            });
        });
    });

    describe('#insertUserLocations()', function() {
        var locsWithDate = [{coordinates: newyorkLoc, date: newyorkDate}, {coordinates: bostonLoc, date: bostonDate}];

        it('should create location items for Boston and New York', function(done) {
            UserLocation.insertUserLocations(userId, locsWithDate, function(err, docs) {
                should.not.exist(err);
                should.exist(docs);
                if (err) {
                    return done(err);
                }
                docs.should.have.lengthOf(locsWithDate.length);
                done();
            });
        });
    });

    describe('#insertUserLocations - ' + MAX_RANDOM_LOCATIONS + ' random locations', function() {
        it('should create ' + MAX_RANDOM_LOCATIONS + ' random location items', function(done) {
            var lonMin = 1223300,
                lonMax = 1223999,
                latMin = 476000,
                latMax = 476999,
                locsWithDate = new Array(MAX_RANDOM_LOCATIONS);

            for (var i = 0; i < MAX_RANDOM_LOCATIONS; i++) {
                var lon = getRandomInt(lonMin, lonMax) / 10000.0;
                var lat = getRandomInt(latMin, latMax) / 10000.0;
                var date = getRandomDate(new Date(START_DATE_STRING),  new Date(END_DATE_STRING));

                locsWithDate[i] = { coordinates: [lon, lat], date: date };
            }

            UserLocation.insertUserLocations(userId, locsWithDate, function(err, docs) {
                should.not.exist(err);
                should.exist(docs);
                if (err) {
                    return done(err);
                }
                docs.should.have.lengthOf(MAX_RANDOM_LOCATIONS);
                done();
            });
        });
    });

    describe('#getUserLocationsByDateRange()', function() {
        it('should return ' + MAX_RANDOM_LOCATIONS + ' location items based on date range of ' + new Date(START_DATE_STRING).toDateString() + ' to ' + new Date(END_DATE_STRING).toDateString(), function(done) {
            UserLocation.getUserLocationsByDateRange(userId, START_DATE_STRING, END_DATE_STRING, MAX_RANDOM_LOCATIONS, function(err, docs) {
                should.not.exist(err);
                should.exist(docs);
                if (err) {
                    return done(err);
                }
                docs.should.have.lengthOf(MAX_RANDOM_LOCATIONS);
                docs[0].location.coordinates.should.have.lengthOf(2);
                docs[MAX_RANDOM_LOCATIONS - 1].location.coordinates.should.have.lengthOf(2);
                done();
            });
        });
    });

    describe('#getUserLocationsNear()', function() {
        it('should return the San Francisco location based on a query for near', function(done) {
            UserLocation.getUserLocationsNear(sanfranciscoLoc, 100 /* meters */, 1000, function(err, docs) {
                should.not.exist(err);
                should.exist(docs);
                if (err) {
                    return done(err);
                }
                docs.should.have.lengthOf(1);
                should.exist(docs[0]);
                should.exist(docs[0].location);
                done();
            });
        });
    });
});

function getRandomInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

// ex: random time between 5/6/1994 12:00 and now
// getRandomDate(new Date(1994, 5, 6, 12), new Date())
function getRandomDate(start, end) {
    var startTime = start.getTime();
    var endTime = end.getTime();

    return new Date(startTime + (Math.random() * (endTime - startTime)));
}
