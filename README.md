
# Log File Parser (CLI)

This is a command-line Java application that parses mixed-format log files and classifies them into three types:

- **APM Logs**: Aggregates performance metrics like CPU, memory, disk, and network usage.
- **Application Logs**: Counts the number of logs by severity (INFO, ERROR, DEBUG, etc.).
- **Request Logs**: Calculates response time percentiles and status code groupings per API route.

## Features

- Implements the **Chain of Responsibility** design pattern.
- Outputs separate JSON files: `apm.json`, `application.json`, and `request.json`.
- Robust error handling for malformed log lines.
- Easily extensible to support additional log types.

## Usage

```bash
java -jar log-parser-cli.jar --file sample_input_logs.txt

## Output

apm.json
application.json
request.json


## Author
Let me know if you want to include a sample log or add build instructions (e.g., how to run with Maven).
