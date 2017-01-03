#include <stdio.h>
#include <stdlib.h>
#include "server.h"



int main(int argc, char *argv[]) {
    struct server* Server;
    int port;
    int numberOfTables;
    if(argc != 3){
        printf("Run with 2 arguments! Port and number of tables (1-20)");
        return 1;
    }
    port = (int)strtol(argv[1], NULL, 10);
    if( port < 1024 ||  port > 65535 ){
        printf("Port must be between 1024 and 65535");
        return 1;
    }
    numberOfTables = (int)strtol(argv[2], NULL, 10);
    if( numberOfTables < 1 ||  numberOfTables > 20 ){
        printf("Number of tables must be between 1 and 20");
        return 1;
    }

    Server = createServer(port, numberOfTables);
    return runServer(Server);
}
