var net = require('net')
var outputPort = 5005
var os = require("os")
var delay = 0; // time in ms!
var LineByLineReader = require('line-by-line')

main();

async function main() {
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

for (let i = 0; i < ports.length; i ++){
  const port = ports[i];
	const client = await createClient(port, i);

	client.on('end', () => {
		console.log("client " + i + " disconnected from server");
	});

	clients.push(client);
}

function createClient(port, index) {
  return new Promise( (resolve, reject) => {
    const client = net.createConnection({port: port}, () => {
  		console.log("client " + index + " connected on port " + port );
      resolve(client);
  	});
  });
}

function writeToClient(client, line, index) {
  return new Promise( (resolve, reject) => {
    console.log("client " + index + " wrote line of " + line.length + " characters." );
    client.write(line + os.EOL, resolve);
  });
}

// read file an send to each client
lr = new LineByLineReader(filePath);

lr.on('line', function (line) {
	// pause emitting of lines...
	lr.pause();

	// ...do your asynchronous line processing..
	setTimeout(async function () {
    for (let i = 0; i < clients.length; i ++) {
      await writeToClient(clients[i], line, i);
    }

		lr.resume();
	}, delay);
});

lr.on('end', function () {
	// All lines are read, file is closed now.
	clients.forEach(function(client, index) {
		client.end();
	});
});
}
