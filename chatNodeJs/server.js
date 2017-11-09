var express = require('express');
var app = express();
var server = require('http').createServer(app).listen(3000);
var io = require('socket.io').listen(server);
var fs = require('fs');

var users = {};
var userID = {};
io.sockets.on('connection', function(client){

    console.log("client connected: " + client.id);

    //If new user connects store it in into a temporary socket list for lookup
    client.on('details',function(data){
    	var userInfo = JSON.parse(data);
    	users[userInfo.name] = client.id;
    	console.log(users);

    });

    // If a new Message is received send it to destination socket
    client.on('Message',function(data){
    	
    	console.log(data);

    	var userMessage = JSON.parse(data);
    	console.log(userMessage.receiver);
    	console.log(users[userMessage.receiver]);
    	if(users[userMessage.receiver]) io.sockets.to(users[userMessage.receiver]).emit('textMessage',data);
    });

    // If new Video received send it to destination socket
    client.on('VideoMessage',function(data,callback){
    	
    	var userMessage = JSON.parse(data);

    	console.log(userMessage.index);
    	console.log(userMessage.name);
    	
    	// check if reciver directory exits
    	if(!fs.existsSync(userMessage.receiver)) fs.mkdirSync(userMessage.receiver);
    	
    	//If it is new data then create a file else append
    	if(userMessage.index == 0){
    		fs.writeFileSync(userMessage.receiver+"/"+userMessage.name, new Buffer(userMessage.data, 'base64'));
    	}
    	else{
    		fs.appendFileSync(userMessage.receiver+"/"+userMessage.name, new Buffer(userMessage.data, 'base64'));
    	}

    	//Send the message to the reciver socket
    	if(users[userMessage.receiver])	io.sockets.to(users[userMessage.receiver]).emit('videoMessage',data);

    });

    // recive acknolegment from the reciver and send it to sender
    client.on('videoIndexAck',function(data){
    	var userMessage = JSON.parse(data);
    	console.log(data);
    	if(users[userMessage.receiver]) io.sockets.to(users[userMessage.receiver]).emit('videoIndexAck',data);
    });

    // notify that the whole video is sent
    client.on('videoAck',function(data){

    	var userMessage = JSON.parse(data);
    	if(users[userMessage.receiver]) io.sockets.to(users[userMessage.receiver]).emit('videoAck',data);    	

    });
});