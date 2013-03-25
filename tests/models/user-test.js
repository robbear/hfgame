// Test user model

var User = require('../../models/user');

var connectionString = 'mongodb://localhost:27017/user-test';
var testDB;

console.log('Calling User.connectForTest');
User.connectForTest(connectionString, function(db, err) {
    if (err) {
        console.log('Failed to connect to user-test: ' + err);
        return;
    }

    testDB = db;
    console.log('Successfully connected to user-test');

    console.log('Calling createUserTest');
    createUserTest(disconnect);
});

function createUserTest(next) {
    console.log('Calling User.createUser');
    User.createUser('user@test.com', 'password', function(user, err) {
        console.log('In callback from User.createUser');
        if (err) {
            console.log('Failed to create user');
        }
        else {
            console.log('Successfully created user');
        }

        if (next) {
            next(process.exit);
        }
    });
}

function disconnect(next) {
    /*
    console.log('Shortcutting disconnect');
    return;
    */

    console.log('In function disconnect. Calling User.disconnectForTest');
    User.disconnectForTest(testDB, function(err) {
        console.log('In callback from User.disconnectFromTest');
        if (err) {
            console.log('Failed to disconnect from and delete user-test');
        }
        else {
            console.log('Successfully disconnected from and deleted user-test');
        }

        if (next) {
            next();
        }
    });
}


