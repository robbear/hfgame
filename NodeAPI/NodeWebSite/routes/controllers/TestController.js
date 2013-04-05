var restify = require('restify'),
    fs = require('fs'),
    logger = require('../../logger/logger'),
    hfConfig = require('../../config/config.js');

exports.testRoute = function(req, res, next) {
    res.send({msg: 'testRoute value'});
    next();
};

exports.testItemRoute = function (req, res, next) {
    res.send({name: req.params.item, value: "Don't you wish we had a database?"});
    next();
};

exports.testPutRoute = function (req, res, next) {
    res.send({name: req.params.name, age: req.params.age});
    next();
};

