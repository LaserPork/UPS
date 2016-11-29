#ifndef INC_1_SERVER_H
#define INC_1_SERVER_H

#include "dynArray.h"

struct server{
    int numberOfTables;
    struct dynUserArray* users;
    struct game** tables;
    struct dynClientArray* threads;
    int port;
};

struct server* createServer(int port, int numberOfTables);

int runServer(struct server* Server);
#endif
