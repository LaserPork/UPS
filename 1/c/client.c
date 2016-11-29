#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <sys/socket.h>
#include <ctype.h>
#include <string.h>
#include <unistd.h>

#include "server.h"
#include "user.h"
#include "game.h"
#include "client.h"

#define		BUFFSIZE	1000



struct client* createClient(struct server* Server, int socket){
    struct client* Client;
    Client = malloc(sizeof(struct client));
    Client->Server = Server;
    Client->socket = socket;

    pthread_create(&Client->tid, NULL, runClient, Client);

    pthread_detach(Client->tid);

    return Client;
}

void* runClient(void * voidClient){
    char* p;
    struct client* Client = (struct client*)voidClient;
    char buf[BUFFSIZE];
    int c_sockfd = Client->socket;
    printf("Server prijal klienta %p\n", (void *)&Client->tid);
    while(1) {
        memset(&buf, 0, sizeof(buf));
        if (read(c_sockfd, buf, BUFFSIZE) == -1) {
            perror("Chyba pri cteni\n");
            break;
        }else {
            p = strtok(buf, "\r\n");
            while (p != NULL){
                printf("Server gets: %s\n", p);
                recieve(Client, p);
                p = strtok(NULL, "\r\n");
            }



        }
    }
    printf("Klient %p ukoncil prubeh\n", (void *)&Client->tid);
    close(c_sockfd);

    return NULL;
}

int recieve(struct client* Client, char* mess){
    int i = 0;
    int size = 0;
    char* type;
    char* p;
    char* array[10];
    char buf_out[BUFFSIZE];
    int found = 1;
    memset(buf_out, 0, sizeof(buf_out));
    for (i = 0; i < 10; ++i) {
        array[i] = malloc(sizeof(char*));
    }

    p = strtok(mess, "~");
    i=0;
    while (p != NULL && i < 10){
        size++;
        strcpy(array[i++], p);
        p = strtok(NULL, "~");
    }

    if(size != atoi(array[0])){
        printf("Corrupted packet: %s\n", mess);
    }


    type = array[1];

    if(strcmp(type,"login") == 0) {
        strcpy(buf_out, login(Client, array[2], array[3]));
    }else if(strcmp(type,"tables") == 0){
        strcpy(buf_out, getTables(Client));
    }else if(strcmp(type,"table") == 0){
        strcpy(buf_out, getTablePlaying(Client, (int)strtol(array[2],(char **)NULL, 10)));
    }else if(strcmp(type,"join") == 0){
        strcpy(buf_out, join(Client, (int)strtol(array[2],(char **)NULL, 10)));
    }else if(strcmp(type,"logout") == 0){
        strcpy(buf_out, logout(Client));
        printf("%s\n",buf_out);
        /*
         TODO interrupt
        */
    }else if(strcmp(type,"players") == 0){
        getPlayers(Client);
    }else if(strcmp(type,"draw") == 0){
        drawCard(Client);
    }else if(strcmp(type,"enough") == 0){
        enough(Client);
    }else{found = 0;}
        /*

        case "players": getPlayers();
            break;
        case "draw": 	drawCard();
            break;
        case "enough": 	enough();
            break;

    if(found == 0){
        i = 0;
        while(1){
            unsigned int c = (unsigned int)(unsigned char)type[i++];
            if (isprint(c) && c != '\\')
                putchar(c);
            else
                printf("\\x%02x", c);
        }
    }
        */
    freeArray(array, 10);
    if(found){
        if(sendMessage(Client, buf_out)){
            return 1;
        }
    }

    return 0;

}

int sendMessage(struct client* Client, char* buf_out){
    int c_sockfd = Client->socket;
    printf("%p Server sends %s", (void *)&Client->tid, buf_out);
    if (send(c_sockfd, buf_out, strlen(buf_out), 0) == -1) {
        perror("Chyba pri zapisu");
        return 1;
    }
    return 0;
}

char* login(struct client* Client, char* name, char* password){
    int i;
    struct user* newUser;
    static char mess[50];
    printf("%s, %s \n", name, password);
    for (i = 0; i < Client->Server->users->arrayPos; ++i) {
        printf("try to print\n");
        printf("%s\n", Client->Server->users->array[i]->name);
        printf("done\n");
        if(strcmp(Client->Server->users->array[i]->name, name) == 0){

            if(Client->Server->users->array[i]->logged){
                strcpy(mess, "3~login~alreadylogged\n");
                return mess;
            }
            if(strcmp(Client->Server->users->array[i]->password, password) == 0){
                Client->Server->users->array[i]->logged = 1;
                Client->currentlyLogged = Client->Server->users->array[i];
                strcpy(mess, "3~login~success\n");
                return mess;
            }
            strcpy(mess, "3~login~failpassword\n");
            return mess;
        }
    }
    newUser = createUser(name, password, Client);
    newUser->logged = 1;
    addUser(Client->Server->users, newUser);
    Client->currentlyLogged = newUser;
    return "3~login~registered\n";
}


char* getTables(struct client* Client){
    char num[10];
    static char mess[50];
    sprintf(num, "%d" , Client->Server->numberOfTables);
    strcpy(mess, "3~tables~");
    strcat(mess, num);
    strcat(mess, "\n");
    return mess;
}

char* getTablePlaying(struct client* Client, int id){
    char num[20];
    static char mess[50];
    sprintf(num, "%d~%d" ,id , Client->Server->tables[id]->playingPos);
    strcpy(mess, "4~table~");
    strcat(mess, num);
    strcat(mess, "\n");
    return mess;
}

char* join(struct client* Client, int id){
    char idStr[10];
    char gameStr[10];
    static char mess[50];
    struct game* Game = Client->Server->tables[id];
    sprintf(idStr, "%d", id);
    if(Game->playingPos == 5){
        strcpy(mess, "4~join~");
        strcat(mess, idStr);
        strcat(mess, "~full\n");
        return mess;
    }
    if(Client->currentlyLogged->game != NULL){
        sprintf(gameStr, "%d", Client->currentlyLogged->game->id);
        strcpy(mess, "5~join~");
        strcat(mess, idStr);
        strcat(mess, "~alreadyplaying~");
        strcat(mess, gameStr);
        strcat(mess, "\n");
        return mess;
    }
    Client->currentlyPlaying = Game;
    joinGameUser(Game, Client->currentlyLogged);
    notifyGameAboutJoin(Game, Client->currentlyLogged);
    strcpy(mess, "4~join~");
    strcat(mess, idStr);
    strcat(mess, "~success\n");
    return mess;
}

char* logout(struct client* Client){
    static char mess[50];
    Client->currentlyLogged->logged = 0;
    Client->currentlyPlaying = NULL;
    strcpy(mess, "3~logout~success\n");
    return mess;
}

void getPlayers(struct client* Client){
    int i;
    char mess[50];
    for (i = 0; i < Client->currentlyPlaying->playingPos; i++) {
        /* nerovnost */
        if(strcmp(Client->currentlyPlaying->playing[i]->name, Client->currentlyLogged->name)){
            strcpy(mess, "3~playerJoined~");
            strcat(mess, Client->currentlyPlaying->playing[i]->name);
            strcat(mess, "\n");
            sendMessage(Client, mess);
        }
    }
}

void drawCard(struct client* Client){
    char num[10];
    char mess[50];
    if(Client->currentlyLogged->hasEnough){
        sendMessage(Client, "3~draw~haveEnough\n");
    }else if(isUserOver(Client->currentlyLogged)){
        sendMessage(Client, "3~draw~areOver\n");
    }else if(Client->currentlyPlaying->deckPos > 0){
        sprintf(num, "%d" , drawUserCard(Client->currentlyLogged));
        strcpy(mess, "4~draw~success~");
        strcat(mess, num);
        strcat(mess, "\n");
        sendMessage(Client, mess);
        notifyGameAboutDraw(Client->currentlyPlaying, Client->currentlyLogged);
    }
    if(isUserOver(Client->currentlyLogged)){
        fold(Client);
    }
    tryToEndGame(Client->currentlyPlaying);
}

void enough(struct client* Client){
    if(!isUserOver(Client->currentlyLogged)){
        userEnough(Client->currentlyLogged);
        sendMessage(Client, "3~enough~success\n");
        notifyGameAboutEnough(Client->currentlyPlaying, Client->currentlyLogged);
    }else{
        sendMessage(Client, "3~enough~areOver\n");
    }
    tryToEndGame(Client->currentlyPlaying);
}

void fold(struct client* Client){
    if(isUserOver(Client->currentlyLogged)){
        notifyGameAboutFold(Client->currentlyPlaying, Client->currentlyLogged);
    }
}