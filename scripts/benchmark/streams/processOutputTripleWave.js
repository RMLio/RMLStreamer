const fs = require('fs');
const WebSocket = require('ws');

// process arguments
const wsPath = process.argv[2];
const outputFile = process.argv[3];
const prefixURL = process.argv[4];
const numberOfExpectedTriples = parseInt(process.argv[5]);

const stream = fs.createWriteStream(outputFile, {flags: 'a'});
const ids = {};

function start() {
  const ws = new WebSocket(wsPath);

  ws.on('error', err => {
    setTimeout(() => {
      start();
    }, 500);
  });

  function appendToFile(data) {
    stream.write(data + '\n');
  }

  ws.on('message', function incoming(data) {
    data = JSON.parse(data)['@graph'];
    const id = data['@id'].replace(prefixURL, '');

    if (!ids[id]) {
      ids[id] = 0;
    }

    ids[id] += Object.keys(data).length - 1;

    if (ids[id] === numberOfExpectedTriples) {
      appendToFile(`${id},${new Date().getTime()}`);
      delete ids[id];
    }
  });
}

start();