var should = require('should'),
    TestUtils = require('../../models/testutils'),
    User = require('../../models/user');
    UserLocation = require('../../models/userlocation');

describe('UserLocation', function() {
    var seattleLoc = {lon: 122.3331, lat: 47.6097};
    var sanfranciscoLoc = {lon: 122.4183, lat: 37.7750};
    var newyorkLoc = {lon: 74.0064, lat: 40.7142};
    var bostonLoc = {lon: 71.0603, lat: 42.3583};
    var bostonDate = new Date("2011-12-06T22:30:00Z");
    var newyorkDate = new Date("2011-05-06T19:00:00Z");
    var MAX_RANDOM_LOCATIONS = 1000;
    var START_DATE_STRING = "2013-01-01T12:00:00Z";
    var END_DATE_STRING = "2013-04-30T12:00:00Z";
    var userId;

    before(function(done) {
        // Ensure the User and UserLocation collections are empty before we run our tests
        TestUtils.dropCollection(User.collectionName, function(err) {
            if (err) {
                return done(err);
            }
            else {
                TestUtils.dropCollection(UserLocation.collectionName, function(err) {
                    User.createUser('testuser@test.com', 'password', function(err, user) {
                        if (err) {
                            return done(err);
                        }
                        else {
                            userId = user.id;
                            done();
                        }
                    });
                });
            }
        });
    });

    describe('#createUserLocation() - Seattle', function() {
        it('should create a location item for Seattle', function(done) {
            UserLocation.createUserLocation(userId, seattleLoc, function(err, userLocation) {
                should.not.exist(err);
                should.exist(userLocation);
                should.exist(userLocation.location);
                should.exist(userLocation.userId);
                if (err) {
                    return done(err);
                }
                else {
                    userLocation.location.should.have.property('lon', seattleLoc.lon);
                    userLocation.location.should.have.property('lat', seattleLoc.lat);
                    userLocation.userId.should.be.a('object');
                    userLocation.userId.toString().should.equal(userId);
                    done();
                }
            });
        });
    });

    describe('#createUserLocation() - San Francisco', function() {
        it('should create a location item for San Francisco', function(done) {
            UserLocation.createUserLocation(userId, sanfranciscoLoc, function(err, userLocation) {
                should.not.exist(err);
                should.exist(userLocation);
                should.exist(userLocation.location);
                should.exist(userLocation.userId);
                if (err) {
                    return done(err);
                }
                else {
                    userLocation.location.should.have.property('lon', sanfranciscoLoc.lon);
                    userLocation.location.should.have.property('lat', sanfranciscoLoc.lat);
                    userLocation.userId.should.be.a('object');
                    userLocation.userId.toString().should.equal(userId);
                    done();
                }
            });
        });
    });

    describe('#insertUserLocations()', function() {
        var locsWithDate = [{location: newyorkLoc, creationDate: newyorkDate}, {location: bostonLoc, creationDate: bostonDate}];

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

                locsWithDate[i] = { location: {lon: lon, lat: lat}, creationDate: date };
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