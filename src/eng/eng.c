#include <unistd.h>
#include <stdlib.h>

#define JAVA "/usr/bin/java"

int main( int argc, char* argv[] ) {

    char** args = (char**)calloc( 3 + argc, sizeof( char* ));

    args[ 0 ] = JAVA;
    args[ 1 ] = "-jar";
    args[ 2 ] = "enguage.jar";

    for (int i=1; i<argc; i++)
      args[ i+2 ] = argv[ i ];

    execv( JAVA, args );
}
