/**
 * author: Pieter Heyvaert (pheyvaer.heyvaert@ugent.be)
 * Ghent University - imec - IDLab
 */

//modules
const parse = require('csv-parse/lib/sync');
const fs    = require('fs');

//arguments
const fileStart   = process.argv[2];
const fileStop    = process.argv[3];
const outputFile  = process.argv[4];

//variables
const fileStartString = fs.readFileSync(fileStart, {encoding: 'utf-8'});
const fileStopString  = fs.readFileSync(fileStop, {encoding: 'utf-8'});

const startRecords = parse(fileStartString, {columns:true});
const stopRecords  = parse(fileStopString, {columns:true});
const stream       = fs.createWriteStream(outputFile, {flags: 'a'});

let totalDelay = 0;
let totalCompletedRecords = 0;

for (let i = 0; i < startRecords.length; i ++) {
  let j = 0;

  while (j < stopRecords.length && stopRecords[j].id !== startRecords[i].id) {
    j ++;
  }

  if (j < stopRecords.length) {
    const difference = parseInt(stopRecords[j].time) - parseInt(startRecords[i].time);
    stream.write(`${startRecords[i].id},${difference}\n`);
    totalCompletedRecords ++;
    totalDelay += difference;
  } else {
    stream.write(`${startRecords[i].id},\n`);
  }
}

const averageDelay = Math.round(totalDelay/totalCompletedRecords);
const droppedRecords = startRecords.length - totalCompletedRecords;

console.log(`${averageDelay},${droppedRecords}`);