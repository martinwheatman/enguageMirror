# Enguage(TM) - (c) Martin Wheatman
A pragmatic utterance mediator: appropriating a context-dependent interpretation.

Usage: java -jar enguage.jar [-d <configDir>] [-p <port> | -s | [--server [<port>]] -t ]
where: -d <configDir>
          config directory, default="./src/assets"

       -p <port>, --port <port>
          listens on local TCP/IP port number

       -c, --client
          runs Engauge as a shell

       --server [<port>]
          switch to send test commands to a server.
          This is only a test, and is on localhost.
          (Needs to be initialised with -p nnnn)

       -t, --test
          runs a sanity check

# Enguage
