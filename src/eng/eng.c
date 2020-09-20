#include <sys/types.h>
#include <sys/stat.h>
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>

#define JAVA "/usr/bin/java"
#define JAR  "lib/enguage.jar"

char* findEng( char* loc ) {
	char jarLoc[ 1024 ];
	snprintf( jarLoc, 1024, "%s/%s", loc, JAR );
	struct stat buf;
	return 0 == stat( jarLoc, &buf) ? strdup( jarLoc ) : NULL;
}
void execEng( char* jar, int argc, char* argv[]) {
	char** args = (char**)calloc( 3 + argc, sizeof( char* ));

	args[ 0 ] = JAVA;
	args[ 1 ] = "-jar";
	args[ 2 ] = jar;

	for (int i=1; i<argc; i++)
		args[ i+2 ] = argv[ i ];

	execv( JAVA, args );
}

int main( int argc, char* argv[] ) {
	char* jarLoc;
	if (jarLoc = findEng( getenv( "HOME" )))
		execEng( jarLoc, argc, argv );
	else if (jarLoc = findEng( "/app" ))
		execEng( jarLoc, argc, argv );
	else
		fprintf( stderr, "Error: can't find JAR file\n" );

	return -1;
}
