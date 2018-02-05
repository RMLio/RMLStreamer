var net = require('net')
var inputPort = 5005
var outputPort = 9000
var os = require("os")
var delay = 0; // time in ms!

    

	
// process file path argument for input data
var filePath = ""
process.argv.forEach(function (val, index, array) {
  if(index==2) {
	inputPort = val;
  }
  if(index==3) {
	delay = val;
  }
});

// create input tcp server
var inputServer = net.createServer(function(socket) {

    console.log("Input Socket connected." + os.EOL + os.EOL)	

	var readline = require('readline');
var rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout,
  terminal: false
});

rl.on('line', function(line){
    socket.write(line + os.EOL);
})

rl.on('close', function() {
    socket.end();
    inputServer.close();
})
	

});

// start tcp server
inputServer.listen(inputPort);

console.log("Opening port " + inputPort + os.EOL)
