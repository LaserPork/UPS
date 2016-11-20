#ifndef INC_1_SERVER_H
#define INC_1_SERVER_H
#define		TABLECNT	5

#include "dynArray.h"

struct server{
    struct dynUserArray* users;
    struct game* tables[TABLECNT];
    struct dynClientArray* threads;
    int port;
};

struct server* createServer(int port, int numberOfTables);

int runServer(struct server* Server);
#endif
