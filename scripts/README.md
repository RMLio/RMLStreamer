### Scripts 

#### Installing scripts

First the necessary dependencies need to be installed in the scripts folder.

```
cd scripts/
npm install
```
#### Using scripts

##### Setting up streaming input from file

Easy setup of a TCP Server that listens to the given port and sends items from a file when an RML Streamer makes a connection. The individual items in the file need to be separated by new lines.

```
cd scripts/
node fileInputStream.js <Input File Location> <Input Port> <Delay in ms>
```

- Input File Location: location of the file that contains the elements
- Input Port: port where the input stream will be send to by TCP
- Delay in ms: send each element with a delay in ms

Input file examples:

```
{ "id" : "12" }
{ "id" : "14" }
{ "id" : "16" }
{ "id" : "18" }
{ "id" : "20" }
{ "id" : "29" }
```

```
<element><id>2</id></element>
<element><id>4</id></element>
<element><id>6</id></element>
<element><id>8</id></element>
<element><id>10</id></element>
<element><id>12</id></element>
```

##### Setting up streaming input from stdin

Easy setup of TCP server that opens a given port and send stdinput to this port when a socket connection has been made.
```
cd scripts/
node standardInputStream.js <Input Port>
```
- Input Port: port where the input stream will be send to by TCP

An example with pipes:
```
cd scripts/
cat inputFile | node standardInputStream.js <Input Port>
```
##### Setting up streaming output to stdout

Easy setup of a TCP server that sends an output stream to standard output over a given port.

```
cd scripts/
node standardOutputStream.js <Output Port>
```
- Output Port: port where the output stream will be send to by TCP
