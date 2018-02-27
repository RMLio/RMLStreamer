var net = require('net')
var inputPort = 5005
var outputPort = 9000
var os = require("os")
var delay = 0; // time in ms!
var LineByLineReader = require('line-by-line')
    

	
// process file path argument for input data
var filePath = ""
process.argv.forEach(function (val, index, array) {
  if(index==2) {
	filePath = val;
  } 
  if(index==3) {
	inputPort = val;
  }
  if(index==4) {
	delay = val;
  }
});

// create input tcp server
var inputServer = net.createServer(function(socket) {

    console.log("Input Socket connected." + os.EOL + os.EOL)	

	lr = new LineByLineReader(filePath);

	lr.on('line', function (line) {
    	// pause emitting of lines...
    	lr.pause();

	    // ...do your asynchronous line processing..
	    setTimeout(function () {
		socket.write(line + os.EOL);
		lr.resume();
	    }, delay);
	});

	lr.on('end', function () {
	    // All lines are read, file is closed now.
		socket.end();
		  inputServer.close();

	});
	

});

// start tcp server
inputServer.listen(inputPort);

console.log("Opening port " + inputPort + os.EOL)
