#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <sys/stat.h>

#define JAVA "/usr/bin/java"
#define JAR  "lib/enguage.jar"

int main( int argc, char* argv[] ) {

	char jarLoc[ 1024 ];
	snprintf( jarLoc, 1024, "%s/%s", getenv( "HOME" ), JAR ); 
	
	struct stat buf;
	if (-1 == stat( jarLoc, &buf)) {
		fprintf( stderr, "Error: can't find JAR file\n" );
	} else {
		char** args = (char**)calloc( 3 + argc, sizeof( char* ));

		args[ 0 ] = JAVA;
		args[ 1 ] = "-jar";
		args[ 2 ] = JAR;

		for (int i=1; i<argc; i++)
			args[ i+2 ] = argv[ i ];

		execv( JAVA, args );
	}
	return -1;
}
