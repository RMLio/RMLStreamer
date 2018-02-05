### Scripts 

#### Installing scripts

First the necessary dependencies need to be installed in the scripts folder.

```
npm install
```
#### Using scripts

##### Setting up streaming input from file

Easy setup of a TCP Server that opens a the given port and sends items from a file when a socket connection has been made. The individual items in the file need to be separated by new lines. When all elements have been read the TCP server will be closed.
```
node streamFromFile.js <Input File Location> <Output Port> <Delay in ms>
```

- Input File Location: location of the file that contains the elements
- Output Port: port where the input stream will be send to by TCP
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
node streamFromStandardInput.js <Output Port>
```
- Output Port: port where the input stream will be send to by TCP

An example with pipes:
```
cat inputFile | node streamFromStandardInput.js <Output Port>
```
##### Setting up streaming output to stdout

Easy setup of a TCP server that sends an input stream to standard output over a given port.

```
node streamToStandardOutput.js <Input Port>
```
- Input Port: port where the output stream will be send to by TCP
