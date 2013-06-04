
var express = require('express'),
    utilities = require('../utilities/utilities');

var app = module.exports = express();

app.get('/contact', function(req, res) {
    utilities.sendOutputHtml("root", req, res, 'views/contact_header.html', 'views/contact_body.html',
        {
            "PageTitle": "Hyperfine Software - Contact"
        });
});