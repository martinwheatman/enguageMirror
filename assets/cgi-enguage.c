#include <stdio.h>
#include <sys/socket.h>
#include <stdlib.h>
#include <unistd.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <string.h>

#define PORT  8080
#define SVR_IP_ADDR "127.0.0.1"

int main( int argc, char* argv[] ){
	int n, ptr = 0, clientSocket;
	char buffer[1024];
	struct sockaddr_in serverAddr;
	socklen_t addr_size;

	memset(buffer, '\0', 1024);
	
	// create the socket
	if (-1 == (clientSocket = socket(PF_INET, SOCK_STREAM, 0))) {
		strcpy( buffer, "socket error" );
	} else {
		// configure the server address
		serverAddr.sin_family = AF_INET;
		serverAddr.sin_port = htons(PORT);
		serverAddr.sin_addr.s_addr = inet_addr(SVR_IP_ADDR);
		memset(serverAddr.sin_zero, '\0', sizeof serverAddr.sin_zero);

		// onnect to the server
		addr_size = sizeof serverAddr;
		
		if (-1 == connect(clientSocket, (struct sockaddr *) &serverAddr, addr_size)) {
			strcpy( buffer, "connect error: enguage server down?" );
		} else {

			// read the args into a string
			if (argc>1) { // GET
				while (--argc && ++argv) {
					// strip '.' from last arg
					if (argc == 1) {
						int len = strlen( argv[ 0 ]);
						if (len && argv[ 0 ][ len - 1 ] == '.')
							argv[ 0 ][ len - 1 ] = '\0';
					}
					strcat( buffer, argv[ 0 ]);
					if (argc>1) strcat( buffer, " " ); // add sep
				}
				strcat( buffer, "\n" );
			} else { // POST
				ptr = 0;
				while (0 < (n = read( 0, buffer+ptr, 1024 - ptr )))
					ptr += n;
			}
			
			// send teh string to the server
			if (-1 == send(clientSocket, buffer, 1024, 0)) {
				strcpy( buffer, "send() error: enguage server down?" );
			} else {
				// read the reply
				memset(buffer, '\0', 1024);
				ptr = 0;
				while (0 < (n = recv(clientSocket, buffer+ptr, 1024 - ptr, 0)))
					ptr += n;
				
				if (-1 == n) strcpy( buffer, "recv() error: enguage server down?" );
	}	}	}
	
	// print it:- http-style
	printf("Content-type: text/plain\n\n%s",buffer);	 
	return 0;
}
