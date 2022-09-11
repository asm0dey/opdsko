# opdsko
OPDS server for FB2 files

## Goal

I have a large collection of fb2 books and want it to be available over HTTP in a nice categorized way.

This server provides two ways of acessing the library: OPDS and HTTP. Both of them support not only navigation, but also search.

## Usage

Usage is pretty simple:

1. You should have Java installed on your machine (minimal version is 11
2. Grab the latest release from the Releases section
3. Run it like this:
    `java -jar opdsko.jar -P:ktor.indexer.path=<path to the directory with your fb2 files>`

## Configuration

1. `-P:ktor.deployment.port=<port>` to launch the server on the specified port (default is 8080)
2. `-P:ktor.indexer.path=<path>` to use specified directory as the base directory to look for `fb2`s

## Scanning (and rescanning)

Since the scanning can be long and resource-intensive process, there is no automatic scanning (at least yet). To launch scan, call the `/scan` endpoint of the server with `POST` method. For example like this:

```
curl -XPOST http://example.com:8080/scan
```

Do not forget to change host and port of the server
