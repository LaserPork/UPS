#include <stdio.h>
#include <stdlib.h>
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
#include "client.h"

#define		BUFFSIZE	1000

void* test(void * voidClient){
    struct client* Client = (struct client*)voidClient;
    while(1) {
        sleep(1);
        printf("Server vidi klienta %p\n", (void *) &Client->tid);
    }
}


struct client* createClient(struct server* Server, int socket){
    struct client* Client;
    Client = malloc(sizeof(struct client));
    Client->Server = Server;
    Client->socket = socket;
    printf("creaaating\n");
    pthread_create(&Client->tid, NULL, test, Client);
    printf("detaching\n");
    pthread_detach(Client->tid);
    return Client;
}

void* runClient(void * voidClient){
    struct client* Client = (struct client*)voidClient;
    char buf[BUFFSIZE];
    int c_sockfd = Client->socket;
    printf("Server prijal klienta %p\n", (void *)&Client->tid);
    while(1) {
        memset(&buf, 0, sizeof(buf));
        if (recv(c_sockfd, buf, BUFFSIZE, 0) == -1) {
            perror("Chyba pri cteni\n");
            break;
        }else {
            printf("Server gets: %s \n", buf);

            /*          */
            if(recieve(Client, buf)){
                break;
            };
            /*          */


        }
    }

    close(c_sockfd);

    return NULL;
}

int recieve(struct client* Client, char* mess){
    int i = 0;
    int size = 0;
    char* type;
    char* p = strtok(mess, "~");
    char* array[10];
    char buf_out[BUFFSIZE];
    int c_sockfd = Client->socket;

    while (p != NULL && i < 10){
        size++;
        array[i++] = p;
        p = strtok(NULL, "~");
    }

    if(size != atoi(array[0])){
        printf("Corrupted packet: %s \n", mess);
    }

    type = array[1];

    if(strcmp(type,"login") == 0){
        strcpy(buf_out, login(Client,array[2],array[3]));
        /*    case "tables": 	out.println(getTables());
            break;
        case "table": 	out.println(getTablePlaying(Integer.parseInt(ar[2])));
            break;
        case "join": 	out.println(join(Integer.parseInt(ar[2])));
            break;
        case "logout": 	out.println(logout());
            Thread.currentThread().interrupt();
            break;
        case "players": getPlayers();
            break;
        case "draw": 	drawCard();
            break;
        case "enough": 	enough();
            break;
            */

        printf("Server sends: %s\n", buf_out);
        if (send(c_sockfd, buf_out, strlen(buf_out), 0) == -1) {
            perror("Chyba pri zapisu");
            return 1;
        }

    }

    return 0;
}

char* login(struct client* Client, char* name, char* password){
    int i;
    struct user* newUser;
    for (i = 0; i < Client->Server->users->arrayPos; ++i) {
        if(strcmp(Client->Server->users->array[i]->name, name)){
            if(Client->Server->users->array[i]->logged){
                return "3~login~alreadylogged\n";
            }
            if(strcmp(Client->Server->users->array[i]->password, password)){
                Client->Server->users->array[i]->logged = 1;
                Client->currentlyLogged = Client->Server->users->array[i];
                return "3~login~success\n";
            }
            return "3~login~failpassword\n";
        }
    }
    newUser = createUser(name, password, &Client->tid);
    newUser->logged = 1;
    addUser(Client->Server->users, newUser);
    Client->currentlyLogged = newUser;
    return "3~login~registered\n";
}