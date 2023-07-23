## API

Currently accepts POST requests with application/json body, does not accept path parameters.

Send a curl request to get a response:

```
curl -X POST -H "Content-Type: application/json" -d '{"sessionId": "12345", "utterance": "what do I need?"}' http://localhost:8080/interpret
you don't need anything
```

## Run Locally

### With Docker

1. Build the ./DockerFile: `docker build ../.. -t enguage -f Dockerfile`
2. Run a Docker Container: `docker run -p 8080:8080 --mount type=volume,src=eng,dst=/var/local/eng --name engage-web-server enguage`

### With Javac

1. Compile: `javac Server.java`
2. Run: `java Server`

## Deploy with Fly.io

Use the `flyctl` CLI (see [docs](https://fly.io/docs/speedrun/)).

Create an account on fly.io and sign in with the CLI: `fly auth login`.

To create a new app: `fly launch`.

To deploy to an existing app: `fly deploy`.
