var restify = require('restify'),
    bunyan = require('bunyan');

var _bunyanLogger = bunyan.createLogger({
    name: 'hfapi',
    streams: [
        {
            level: 'trace',
            path: './logfiles/hfapi-info.log',
            type: 'rotating-file',
            period: '1d',
            count: 10
        },
        {
            level: 'error',
            path: './logfiles/hfapi-errors.log',
            type: 'rotating-file',
            period: '1d',
            count: 10
        },
        {
            stream: process.stdout,
            level: 'trace'
        }
    ],
    //serializers: restify.bunyan.serializers
    serializers: {
        req: restify.bunyan.serializers.req,
        res: resSerializer,
        err: restify.bunyan.serializers.err
    }
});

exports.bunyanLogger = function () {
    return _bunyanLogger;
}

/* NEVER
function reqSerializer(req) {
    if (!req || !req.connection)
        return req;
    return {
        req_id: req._id,
        method: req.method,
        url: req.url,
        headers: req.headers,
        remoteAddress: req.connection.remoteAddress,
        remotePort: req.connection.remotePort
    };
}
*/

function resSerializer(res) {
    if (!res || !res.statusCode)
        return res;
    return {
        req_id: res.req._id,
        statusCode: res.statusCode,
        header: res._header
    }
}