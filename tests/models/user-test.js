// Test user model

var User = require('../../models/user');

var connectionString = 'mongodb://localhost:27017/user-test';

User.connect(connectionString, function(err) {
    if (err) throw err;
    console.log('Successfully connected to MongoDB');
});
