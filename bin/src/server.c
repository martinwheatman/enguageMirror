#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>
#include <string.h>
#include <ctype.h>
#include <sys/types.h>

#define HTTP_REPLY	\
	"HTTP/1.1 200 OK\r\n\r\n" \
	"<!DOCTYPE html>\r\n<html><body>" \
	"This is not the webserver that you're looking for... ;-)"\
	"</body></html>"

long unsigned myread(int fd, char *buffer, int space) {
	char *cp = buffer;
	int leadingSpaces = 1, r;
	errno = 0;
	while (space>0 && (r = recv(fd, cp, 1, MSG_DONTWAIT)) != 0) {
		if (*cp != ' ' || !leadingSpaces) {
			printf( "rx: %c (%d=%s)\n", isprint(*cp) ? *cp : '*', errno, strerror( errno ));
			if (errno != 11) {
				leadingSpaces = 0;
				space--;
				cp++;
			} else
				break;
		}
		errno = 0;
	}
	if (!r) printf( "null return\n" );
	return cp - buffer;
}

int readn( int sd, char* ptr, int nbytes) {
	int rd = 0, nleft = nbytes;
	
	while (nleft > 0) {
		printf( "going to read\n" );
		rd = myread(sd, ptr, nleft);
		if (rd < 0) {
			printf( "error %d %s\n", errno, strerror( errno ));
			return rd;
		} else if (rd == 0) {
			printf( "break 0\n" );
			break;
		} else
			printf( "read %d\n", rd );
		nleft -= rd;
		ptr   += rd;
	}
	return nbytes - nleft;
}
		
int main(int argc, char *argv[])
{
	int sd = 0, cd = 0;
	struct sockaddr_in serv_addr; 
	char sendBuff[1024], getBuff[1024];
	int port = argc>1 ? atoi(argv[1]) : 8080;

	sd = socket(AF_INET, SOCK_STREAM, 0);
	memset(&serv_addr, '0', sizeof(serv_addr));

	serv_addr.sin_family = AF_INET;
	serv_addr.sin_addr.s_addr = htonl(INADDR_ANY);
	serv_addr.sin_port = htons(port); 

	bind(sd, (struct sockaddr*)&serv_addr, sizeof(serv_addr)); 
	listen(sd, 10); 

	while(-1 != (cd = accept(sd, (struct sockaddr*)NULL, NULL)))
		if (!fork()) {

			memset(getBuff, ' ', sizeof(getBuff));
			int r = myread( cd, getBuff, sizeof( getBuff )-1 );
			printf( ">>>\n%.*s\n>>>\n", r, getBuff );
	
			snprintf(sendBuff,
						sizeof(sendBuff),
						!strncmp( "GET ", getBuff, 4 ) ?
					 			HTTP_REPLY
					 			: !r ? "failure" : "success"
			);
			
			printf( "<<<\n%s\n<<<\n", sendBuff );
			write( cd, sendBuff, strlen( sendBuff ));
			exit( 0 );
		} else
			close(cd);
}
