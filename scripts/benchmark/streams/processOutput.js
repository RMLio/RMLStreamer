const net = require('net');
const os = require('os');
const N3 = require('n3');
const fs = require('fs');

// process arguments
const outputPort = process.argv[2];
const outputFile = process.argv[3];
const prefixURL = process.argv[4];
const numberOfExpectedTriples = parseInt(process.argv[5]);

const stream = fs.createWriteStream(outputFile, {flags: 'a'});
const ids    = {};

function appendToFile(data) {
  stream.write(data + '\n');
}

// create output tcp server
const outputServer = net.createServer(function (socket) {

  socket.on("data", function (data) {
    //console.log(data.toString());
    const parser = new N3.Parser();

    parser.parse(data.toString(), (err, triple) => {
      if (triple) {
        const id = triple.subject.replace(prefixURL, '');

        if (!ids[id]) {
          ids[id] = 0;
        }

        ids[id] = ids[id] + 1;

        if (ids[id] === numberOfExpectedTriples) {
          appendToFile(`${id},${new Date().getTime()}`);
          delete ids[id];
        }
      } else if (err) {
        console.log(err);
      }
    });
  });
});

// start tcp server
outputServer.listen(outputPort);
console.log(`Listening to ${outputPort}.${os.EOL}`);
