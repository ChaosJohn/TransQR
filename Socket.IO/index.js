var app = require('express')(); 
var http = require('http').Server(app); 
var io = require('socket.io')(http); 

app.get('/', function(req, res) {
  //res.send('<h1>hello world</h1>'); 
  res.sendFile(__dirname + '/index.html'); 
}); 

io.on('connection', function(socket) {
  console.log('a user connected'); 
  socket.on('disconnect', function() {
    console.log('user disconnected'); 
  }); 
  socket.on('chat message', function(msg){
    console.log('message: ' + msg);
    socket.broadcast.emit('response', msg); 
    //socket.emit('response', 'Got it: ' + msg); 
  });
  socket.on('error', function(err) {
    if (err.description) throw err.description; 
    else throw err; 
  }); 
}); 

http.listen(3000, function() {
  console.log('listening on *:3000'); 
}); 
