var net = require('net')
var outputPort = 9000;
var os = require("os");
	
// process arguments
process.argv.forEach(function (val, index, array) {
  if(index==2) {
	outputPort = val;
  } 
});

// create output tcp server
var outputServer = net.createServer(function(socket) {

    console.log("Output Socket connected." + os.EOL)	

    socket.on("data", function(data) {
	console.log(data.toString());
    });

});

// start tcp server
outputServer.listen(outputPort);

console.log("Listening on ports " + outputPort + os.EOL)
