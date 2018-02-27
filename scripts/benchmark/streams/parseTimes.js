/**
 * author: Pieter Heyvaert (pheyvaer.heyvaert@ugent.be)
 * Ghent University - imec - IDLab
 */

//modules
const parseSync = require('csv-parse/lib/sync');
const parse = require('csv-parse');

const fs    = require('fs');

//arguments
const fileStart   = process.argv[2];
const fileStop    = process.argv[3];
const outputFile  = process.argv[4];

//variables
const fileStopString  = fs.readFileSync(fileStop, {encoding: 'utf-8'});

const stopRecords  = parseSync(fileStopString, {columns:true});
const stream       = fs.createWriteStream(outputFile, {flags: 'a'});
const input = fs.createReadStream(fileStart, {encoding: 'utf-8'});

const parser = parse({columns:true});

let totalDelay = 0;
let totalCompletedRecords = 0;
let totalRecords = 0;

parser.on('readable', function(){
  let record;

  while(record = parser.read()){
    totalRecords ++;
    let j = 0;

    while (j < stopRecords.length && stopRecords[j].id !== record.id) {
      j ++;
    }

    if (j < stopRecords.length) {
      const difference = parseInt(stopRecords[j].time) - parseInt(record.time);
      stream.write(`${record.id},${difference}\n`);
      totalCompletedRecords ++;
      totalDelay += difference;
      stopRecords.splice(j, 1);
    } else {
      stream.write(`${record.id},\n`);
    }
  }
});

parser.on('end', () => {
  const averageDelay = Math.round(totalDelay/totalCompletedRecords);
  const droppedRecords = totalRecords - totalCompletedRecords;

  console.log(`${averageDelay},${droppedRecords}`);
});

input.pipe(parser);
