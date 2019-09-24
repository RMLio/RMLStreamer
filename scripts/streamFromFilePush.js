var net = require('net')
var outputPort = 5005
var os = require("os")
var delay = 0; // time in ms!
var LineByLineReader = require('line-by-line')
    
// process file path argument for input data
var filePath = ""
var ports = [];
process.argv.forEach(function (val, index, array) {
  if(index==2) {
	filePath = val;
  } else if(index==3) {
	delay = val;
  } else if (index > 3) {
	ports.push(val);
  }
});

// set the default port when no port is given
if (ports.length == 0) {
	console.log("No ports given, so assuming " + outputPort);
	ports.push(outputPort);
}

// create output tcp client for each port
var clients = [];
ports.forEach(function(port, index) {
	const client = net.createConnection({port: port}, () => {
		console.log("client " + index + " connected on port " + port )
	});
	client.on('end', () => {
		console.log("client " + index + " disconnected from server");
	});
	clients.push(client);
});

// read file an send to each client
lr = new LineByLineReader(filePath);

lr.on('line', function (line) {
	// pause emitting of lines...
	lr.pause();
	
	// ...do your asynchronous line processing..
	setTimeout(function () {
		clients.forEach(function(client, index) {
			client.write(line + os.EOL);
		});
		lr.resume();
	}, delay);
});

lr.on('end', function () {
	// All lines are read, file is closed now.
	clients.forEach(function(client, index) {
		client.end();
	});
});
