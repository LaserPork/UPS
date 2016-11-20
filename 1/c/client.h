#ifndef INC_1_CLIENT_H
#define INC_1_CLIENT_H
#include <pthread.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <string.h>
#include <sys/types.h>
#include <unistd.h>
#include <errno.h>
#include <w32api/_timeval.h>
#include "server.h"
#include "user.h"
#include "game.h"

struct client{
    int socket;
    pthread_t tid;
    struct server* Server;
    struct user* currentlyLogged;
    struct game* currentlyPlaying;
};

struct client* createClient(struct server* Server, int socket);

void* runClient(void * voidClient);

int recieve(struct client* Client, char* mess);

char* login(struct client* Client, char* name, char* password);

#endif
