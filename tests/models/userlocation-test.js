var should = require('should'),
    TestUtils = require('../../models/testutils'),
    User = require('../../models/user');
    UserLocation = require('../../models/userlocation');

describe('UserLocation', function() {
    var seattleLoc = {lon: 122.3331, lat: 47.6097};
    var sanfranciscoLoc = {lon: 122.4183, lat: 37.7750};
    var newyorkLoc = {lon: 74.0064, lat: 40.7142};
    var bostonLoc = {lon: 71.0603, lat: 42.3583};
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
        var locsWithDate = [{location: newyorkLoc, creationDate: Date.now()}, {location: bostonLoc, creationDate: Date.now()}];

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
});
