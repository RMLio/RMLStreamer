### Scripts 

#### Installing scripts
```
cd scripts/
npm install
```
#### Using scripts

##### Setting up streaming input from file

Easy setup of a TCP Server that listens on a given port and sends items from a file over a socket when an RML Streamer makes a connection. The individual items in the file need to be separated by new lines.

```
cd scripts/
node fileInputStream.js <Input File Location> <Input Port> <Delay in ms>
```

##### Setting up streaming output to stdout

Easy setup of a TCP server that sends an output stream of the RML Streamer to standard output.

```
cd scripts/
node standardOutputStream.js <Output Port>
```
