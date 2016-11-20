#include <stdio.h>
#include <stdlib.h>
#include "server.h"



int main(int argc, char *argv[]) {
    struct server* Server;
    (void)argc;
    (void)argv;
    Server = createServer(1234, 5);
    return runServer(Server);
}
