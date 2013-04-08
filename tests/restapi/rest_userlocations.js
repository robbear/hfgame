var restify = require('restify'),
    should = require('should');

var client = restify.createJsonClient({
    version: '*',
    url: "http://localhost:1338"
});

var seattleCoordinates = [122.3331, 47.6097];
var username = 'restuserlocation@test.com';
var password = 'password';
var userId;

// Constants
var MAX_RANDOM_LOCATIONS = 1000;
var START_DATE_STRING = "2013-01-01T12:00:00Z";
var END_DATE_STRING = "2013-04-30T12:00:00Z";


describe('UserLocations REST API', function() {

    describe('/users/createuser', function() {
        it('should create a test user with { username: \'' + username + '\', password: \'' + password + '\' }', function(done) {
            // BUGBUG
            // TODO: Client model
            client.post('/users/createuser', { username: username, password: password }, function(err, req, res, user) {
                should.not.exist(err);
                should.exist(user);
                if (err || !user) {
                    return done(err);
                }

                res.statusCode.should.equal(200);
                user.should.have.property('username');
                user.should.have.property('id');
                user.username.should.equal(username);
                user.id.should.be.a('string');
                return done();
            });
        });
    });

    describe('/users/login', function() {
        it('should allow a login for user { username: \'' + username + '\', password: \'' + password + '\' }', function(done) {
            // BUGBUG
            // TODO: Client model
            var url = '/users/login?username=' + username + '&password=' + password;
            client.get(url, function(err, req, res, user) {
                should.not.exist(err);
                should.exist(user);
                if (err || !user) {
                    return done(err);
                }

                res.statusCode.should.equal(200);
                user.should.have.property('username');
                user.should.have.property('id');
                user.username.should.equal(username);
                user.id.should.be.a('string');
                userId = user.id;
                return done();
            });
        });
    });

    describe('/userlocations/createlocation', function() {
        it('should create a user location item', function(done) {
            var date = Date.now();
            // BUGBUG
            // TODO: Client model
            client.post('/userlocations/createlocation', { userId: userId, coordinates: seattleCoordinates, date: date }, function(err, req, res, userLocation) {
                should.not.exist(err);
                should.exist(userLocation);
                if (err || !userLocation) {
                    return done(err);
                }

                res.statusCode.should.equal(200);
                userLocation.should.have.property('userId');
                userLocation.should.have.property('coordinates');
                userLocation.should.have.property('date');
                userLocation.userId.should.be.a('string');
                userLocation.coordinates.should.have.lengthOf(2);
                userLocation.date.should.be.a('string');
                userLocation.userId.should.equal(userId);
                userLocation.coordinates[0].should.equal(seattleCoordinates[0]);
                userLocation.coordinates[1].should.equal(seattleCoordinates[1]);
                userLocation.date.should.equal(new Date(date).toJSON());
                return done();
            });
        });
    });

    describe('/userlocations/createlocations', function() {
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

                // BUGBUG
                // TODO: Client model
                locsWithDate[i] = { coordinates: [lon, lat], date: date };
            }

            client.post('/userlocations/createlocations', { userId: userId, locations: locsWithDate }, function(err, req, res, docs) {
                should.not.exist(err);
                should.exist(docs);
                if (err || !docs) {
                    return done(err);
                }

                // BUGBUG
                // TODO: Decide on docs return contents
                return done();
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
