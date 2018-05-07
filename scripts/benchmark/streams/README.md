## Requirements
- Docker

## Usage
- build docker image: `docker build -t stream-scripts .`
- create input stream: `docker run --rm -p 5005:5005 -v $(pwd):/data stream-scripts streamFromFile.js /data/persons.json 5005 0 name /data/in.csv`
- process output stream: 