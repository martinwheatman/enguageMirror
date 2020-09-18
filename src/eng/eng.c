#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <sys/stat.h>

#define JAVA "/usr/bin/java"
#define JAR  "/Users/martinwheatman/lib/enguage.jar"

int main( int argc, char* argv[] ) {

    struct stat buf;
    if (-1 == stat( JAR, &buf)) {
        fprintf( stderr, "Error: can't find JAR file\n" );
        exit( -1 );
    } else {
		char** args = (char**)calloc( 3 + argc, sizeof( char* ));

		args[ 0 ] = JAVA;
		args[ 1 ] = "-jar";
		args[ 2 ] = JAR;

		for (int i=1; i<argc; i++)
		  args[ i+2 ] = argv[ i ];

		execv( JAVA, args );
}	}
