const net = require('net');
const os = require("os");
const LineByLineReader = require('line-by-line');
const JsonSocket = require('json-socket');
const fs = require('fs');

let inputPort = 5005;
let delay = 0; // time in ms!
let initialWait = 0;
let id;
let outputFile;

// process file path argument for input data
let filePath = "";
process.argv.forEach(function (val, index, array) {
  if (index === 2) {
    filePath = val;
  } else if (index === 3) {
    inputPort = val;
  } else if (index === 4) {
    delay = val;
  } else if (index === 5) {
    id = val;
  } else if (index === 6) {
    outputFile = val;
  } else if (index === 7) {
    initialWait = val;
  }
});

const stream = fs.createWriteStream(outputFile, {flags: 'a'});

function appendToFile(data) {
  stream.write(data + '\n');
}

// create input tcp server
const server = net.createServer(function (socket) {
  setTimeout(() => {
    socket = new JsonSocket(socket);

    const lr = new LineByLineReader(filePath);

    lr.on('line', function (line) {
      let lineID;

      if (id) {
        lineID = JSON.parse(line)[id];
      }

      lr.pause();

      setTimeout(function () {
        socket.sendMessage(JSON.parse(line));
        appendToFile(`${lineID},${new Date().getTime()}`);
        lr.resume();
      }, delay);
    });

    lr.on('end', function () {
      socket.sendMessage({status: 'done'});
      socket.end();
      server.close();
    });
  }, initialWait);
});

server.listen(inputPort);

console.log("Opening port " + inputPort + os.EOL);
