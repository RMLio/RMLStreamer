### Scripts 

#### Installing scripts

First the necessary dependencies need to be installed in the scripts folder.

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

- Input File Location: location of the file that contains the elements
- Input Port: port where the input stream will be send to by TCP
- Delay in ms: send each element with a delay in ms.

##### Setting up streaming output to stdout

Easy setup of a TCP server that sends an output stream to standard output over a given port.

```
cd scripts/
node standardOutputStream.js <Output Port>
```
- Output Port: port where the output stream will be send to by TCP
