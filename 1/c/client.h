#ifndef INC_1_CLIENT_H
#define INC_1_CLIENT_H
#include <pthread.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <string.h>
#include <unistd.h>
#include <errno.h>
#include "server.h"
#include "user.h"
#include "game.h"

struct client{
    int socket;
    pthread_t tid;
    int running;
    int shouldDie;
    pthread_t checkerTid;
    struct server* Server;
    struct user* currentlyLogged;
};


struct client* createClient(struct server* Server, int socket);

void* runClient(void * voidClient);

void* runChecker(void * voidClient);

int recieve(struct client* Client, char* mess);

char* login(struct client* Client, char* name, char* password);

char* getTables(struct client* Client);

char* getTablePlaying(struct client* Client, int id);

char* join(struct client* Client, int id);

char* logout(struct client* Client);

char* returnBack(struct client* Client);

int sendMessage(struct client* Client, char* buf_out);

void getPlayers(struct client* Client);

void drawCard(struct client* Client);

void enough(struct client* Client);

void fold(struct client* Client);

char* checkPlayers(struct client* Client);

void checkCards(struct client* Client);

#endif
