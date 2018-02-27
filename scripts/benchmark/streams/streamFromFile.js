const net              = require('net');
const os               = require("os");
const fs               = require('fs');
const LineByLineReader = require('line-by-line');

let inputPort = 5005;
let delay     = 0; // time in ms!

// process file path argument for input data
let filePath = "";
let outputFile;
let id;

process.argv.forEach(function (val, index, array) {
  if (index === 2) {
    filePath = val;
  } else if (index === 3) {
    inputPort = val;
  } else if (index === 4) {
    delay = parseInt(val);
  } else if (index === 5) {
    id = val;
  } else if (index === 6) {
    outputFile = val;
  }
});

const stream = fs.createWriteStream(outputFile, {flags: 'a'});

function appendToFile(data) {
  stream.write(data + '\n');
}

// create input tcp server
const inputServer = net.createServer(function (socket) {
  console.log("Input Socket connected." + os.EOL + os.EOL);
  const lr = new LineByLineReader(filePath);

  lr.on('line', function (line) {
    let lineID;

    if (id) {
      lineID = JSON.parse(line)[id];
    }

    // pause emitting of lines...
    lr.pause();

    // ...do your asynchronous line processing..
    setTimeout(function () {
      socket.write(line + os.EOL);
      appendToFile(`${lineID},${new Date().getTime()}`);
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
console.log("Opening port " + inputPort + os.EOL);