/**
 * author: Pieter Heyvaert (pheyvaer.heyvaert@ugent.be)
 * Ghent University - imec - IDLab
 */

//modules
const parseSync = require('csv-parse/lib/sync');
const parse = require('csv-parse');
const fs    = require('fs');

//arguments
const inputFile   = process.argv[2];
const outputFile  = process.argv[3];

//variables
const stream       = fs.createWriteStream(outputFile, {flags: 'a'});
const input        = fs.createReadStream(inputFile, {encoding: 'utf-8'});
const inputParser  = parse({columns:true});

let totalDelay            = 0;
let totalCompletedRecords = 0;
let totalRecords          = 0;


inputParser.on('end', () => {
  const averageDelay = Math.round(totalDelay/totalCompletedRecords);
  const droppedRecords = totalRecords - totalCompletedRecords;

  console.log(`${averageDelay},${droppedRecords}`);
});

let previousRecord;

inputParser.on('data', (record) => {
  if (previousRecord) {
    if (previousRecord.id === record.id) {
      const difference = parseInt(record.time) - parseInt(previousRecord.time);
      stream.write(`${record.id},${difference}\n`);
      totalCompletedRecords++;
      totalDelay += difference;
      previousRecord = null;
    } else {
      stream.write(`${previousRecord.id},\n`);
      previousRecord = record;
    }
  } else {
    previousRecord = record;
    totalRecords ++;
  }
});

input.pipe(inputParser);
