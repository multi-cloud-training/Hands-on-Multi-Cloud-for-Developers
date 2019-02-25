var express = require('express');
var app = express();

app.use(express.static('./'));

var cache = {};

app.post('/set', function(req, res) {
  var query = req.query;
  Object.keys(query).forEach(function(key) {
    cache[key] = query[key];
  });
  res.status(200).end();
});

app.get('/get', function(req, res) {
  res.send(cache[req.query.key]);
});

var port = 8080;
app.listen(port,function(){
	console.log("start Express server port:%d mode:%s",port,app.settings.env)
});

module.exports = app;
