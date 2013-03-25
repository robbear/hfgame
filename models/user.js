var mongoose = require('mongoose'),
    bcrypt = require('bcrypt');

var Schema = mongoose.Schema,
    SALT_WORK_FACTOR = 10,
    // Allow MAX_LOGIN_ATTEMPTS before locking the account
    MAX_LOGIN_ATTEMPTS = 10,
    // Lock the account for LOCK_TIME seconds to prevent automated brute-force attempts
    LOCK_TIME = 60 * 1000;

var UserSchema = new Schema({
    username: { type: String, required: true, index: { unique: true } },
    password: { type: String, required: true },
    loginAttempts: { type: Number, required: true, default: 0 },
    lockUntil: { type: Number }
});

UserSchema.virtual('isLocked').get(function() {
    // Check for a future lockUntil timestamp
    // Note: !! forces truthy to boolean
    return !!(this.lockUntil && this.lockUntil > Date.now());
});

UserSchema.pre('save', function(next) {
    var user = this;

    // Hash the password only if it has been modified or is new
    if (!user.isModified('password')) {
        return next();
    }

    // Generate a salt
    bcrypt.genSalt(SALT_WORK_FACTOR, function(err, salt) {
        if (err) {
            return next(err);
        }

        // Hash the password using our salt
        bcrypt.hash(user.password, salt, function(err, hash) {
            if (err) {
                return next(err);
            }

            // Override the cleartext password with the hashed one
            user.password = hash;
            next();
        });
    });
});

UserSchema.methods.comparePassword = function(candidatePassword, cb) {
    bcrypt.compare(candidatePassword, this.password, function(err, isMatch) {
        if (err) {
            return cb(err);
        }

        cb(null, isMatch);
    });
};

UserSchema.methods.incLoginAttempts = function(cb) {
    // If we have a previous lock that has expired, restart at 1
    if (this.lockUntil && this.lockUntil < Date.now()) {
        return this.update({
            $set: { loginAttempts: 1 },
            $unset: { lockUntil: 1 }
        }, cb);
    }

    // Otherwise we're incrementing the loginAttempts
    var updates = { $inc: { loginAttempts: 1 } };

    // Lock the account if we've reached max attempts and it's not locked already
    if (!this.isLocked && (this.loginAttempts + 1 >= MAX_LOGIN_ATTEMPTS)) {
        updates.$set = { lockUntil: Date.now() + LOCK_TIME };
    }

    return this.update(updates, cb);
};

//
// For testing purposes: connect method
//
UserSchema.statics.connect = function(connectionString, cb) {
    mongoose.connect(connectionString, function(err) {
        if (err) {
            return cb(err);
        }

        return cb();
    });
};

var reasons = UserSchema.statics.failedLogin = {
    NOT_FOUND: 0,
    PASSWORD_INCORRECT: 1,
    MAX_ATTEMPTS: 2
};

UserSchema.statics.getAuthenticated = function(username, password, cb) {
    this.findOne({ username: username }, function(err, user) {
        if (err) {
            return cb(err);
        }

        // Make sure the user exists
        if (!user) {
            return cb(null, null, reasons.NOT_FOUND);
        }

        // Check if the account is currently locked
        if (user.isLocked) {
            // Increment the login attempts if the account is already locked
            return user.incLoginAttempts(function(err) {
                if (err) {
                    return cb(err);
                }

                return cb(null, null, reasons.MAX_ATTEMPTS);
            });
        }

        // Test for a matching password
        user.comparePassword(password, function(err, isMatch) {
            if (err) {
                return cb(err);
            }

            // Check if the password is a match
            if (isMatch) {
                // If there's no lock or failed attempts, return the user
                if (!user.loginAttempts && !user.lockUntil) {
                    return cb(null, user);
                }

                // Reset attempts and lock info
                var updates = {
                    $set: { loginAttempts: 0 },
                    $unset: { lockUntil: 1 }
                };

                return user.update(updates, function(err) {
                    if (err) {
                        return cb(err);
                    }

                    return cb(null, user);
                });
            }
            else {
                // Password is incorrect, so increment login attempts before responding
                user.incLoginAttempts(function(err) {
                    if (err) {
                        return cb(err);
                    }

                    return cb(null, null, reason.PASSWORD_INCORRECT);
                });
            }
        });
    });
};

module.exports = mongoose.model('User', UserSchema);
