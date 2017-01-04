#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <sys/socket.h>
#include <string.h>
#include <unistd.h>

#include "server.h"


#define		BUFFSIZE	1000



struct client* createClient(struct server* Server, int socket){
    struct client* Client;
    Client = malloc(sizeof(struct client));
    Client->currentlyLogged = NULL;
    Client->Server = Server;
    Client->socket = socket;
    Client->shouldDie = 0;
    Client->recievedMessages = 0;
    Client->closable = 2;
    pthread_create(&Client->tid, NULL, runClient, Client);
    pthread_detach(Client->tid);

    pthread_create(&Client->checkerTid, NULL, runChecker, Client);
    pthread_detach(Client->checkerTid);

    return Client;
}

void* runClient(void * voidClient){
    char* p;
    struct client* Client = (struct client*)voidClient;
    char buf[BUFFSIZE];
    int c_sockfd = Client->socket;
    Client->running = 1;
    printf("Server prijal klienta %p\n", (void *)&Client->tid);
    while(Client->running) {
        memset(&buf, 0, sizeof(buf));
        if (read(c_sockfd, buf, BUFFSIZE) == -1) {
        /*    perror("Chyba pri cteni\n"); */
            break;
        }else {
            Client->shouldDie = 0;
            p = strtok(buf, "\r\n");
            while (p != NULL && Client->running){
                printf("%p Server gets: %s\n", (void *) &Client->tid, p);
                if(recieve(Client, p)){
                    Client->running = 0;
                    break;
                }
                p = strtok(NULL, "\r\n");
            }



        }
    }
    if(Client->currentlyLogged != NULL) {
        logout(Client);
        Client->currentlyLogged->Client = NULL;
    }
    printf("Klient %p ukoncil prubeh\n", (void *)&Client->tid);

    close(c_sockfd);
    Client->closable--;
    if(!Client->closable){
        free(Client);
    }
    return NULL;
}

void* runChecker(void * voidClient){
    struct client* Client = (struct client*)voidClient;
    int i;

    Client->shouldDie = 1;
    for (i = 0; i < 10; ++i) {
        sleep(1);
            if(Client->running) {
                if (!Client->shouldDie) {
                    i = 0;
                    Client->shouldDie = 1;
                }
            }else{
                break;
            }
    }
    logout(Client);
    Client->running = 0;
    Client->closable--;
    if(!Client->closable){
        free(Client);
    }
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
    Client->recievedMessages++;
    memset(buf_out, 0, sizeof(buf_out));
    for (i = 0; i < 10; ++i) {
        array[i] = malloc(sizeof(char) * 20);
    }

    p = strtok(mess, "~");
    i=0;
    while (p != NULL && i < 10){
        size++;
        strncpy(array[i++], p, 19);
        p = strtok(NULL, "~");
    }

    if(size != atoi(array[0])){
        sendMessage(Client, "2~corruptedPacket\n");
        if(Client->recievedMessages == 1){
            return 1;
        }else{
            return 0;
        }
    }

        type = array[1];
    if(Client->recievedMessages == 1 && strcmp(type, "login")){
        return  1;
    }

        if (strcmp(type, "login") == 0) {
            strcpy(buf_out, login(Client, array[2], array[3]));
        } else if (strcmp(type, "tables") == 0) {
            strcpy(buf_out, getTables(Client));
        } else if (strcmp(type, "table") == 0) {
            strcpy(buf_out, getTablePlaying(Client, (int) strtol(array[2], (char **) NULL, 10)));
        } else if (strcmp(type, "join") == 0) {
            strcpy(buf_out, join(Client, (int) strtol(array[2], (char **) NULL, 10)));
        } else if (strcmp(type, "logout") == 0) {
            strcpy(buf_out, logout(Client));
            sendMessage(Client, buf_out);
            return 1;
        } else if (strcmp(type, "players") == 0) {
            getPlayers(Client);
            for (i = 0; i < Client->currentlyLogged->game->playingPos; i++) {
                notifyGameAboutDraw(Client->currentlyLogged->game, Client->currentlyLogged->game->playing[i]);
            }
            for (i = 0; i < Client->currentlyLogged->game->playingPos; i++) {
                if (!Client->currentlyLogged->game->playing[i]->active) {
                    notifyGameAboutLeave(Client->currentlyLogged->game, Client->currentlyLogged->game->playing[i]);
                }
                if (Client->currentlyLogged->game->playing[i]->hasEnough) {
                    notifyGameAboutEnough(Client->currentlyLogged->game, Client->currentlyLogged->game->playing[i]);
                }
            }
            sendMessage(Client, "3~players~success\n");
        } else if (strcmp(type, "draw") == 0) {
            drawCard(Client);
        } else if (strcmp(type, "enough") == 0) {
            enough(Client);
        } else if (strcmp(type, "return") == 0) {
            strcpy(buf_out, returnBack(Client));
        } else if (strcmp(type, "checkCards") == 0) {
            checkCards(Client);
        } else if (strcmp(type, "checkPlayers") == 0) {
            strcpy(buf_out, checkPlayers(Client));
        } else { found = 0; }
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
        if (found) {
            if (sendMessage(Client, buf_out)) {
                return 1;
            }
        }

    return 0;

}

int sendMessage(struct client* Client, char* buf_out){
    int c_sockfd;
    if(Client != NULL){
        c_sockfd = Client->socket;
        if(strlen(buf_out)>0) {
            printf("%p Server sends %s", (void *) &Client->tid, buf_out);
        }
        if (send(c_sockfd, buf_out, strlen(buf_out), 0) == -1) {
        /*    perror("Chyba pri zapisu\n"); */
            return 1;
        }
    }
    return 0;
}

char* login(struct client* Client, char* name, char* password){
    int i;
    struct user* newUser;
    static char mess[50];
    if(Client != NULL) {
        if(Client->currentlyLogged == NULL) {
            for (i = 0; i < Client->Server->users->arrayPos; ++i) {
                if (strncmp(Client->Server->users->array[i]->name, name, 19) == 0) {

                    if (Client->Server->users->array[i]->logged) {
                        strcpy(mess, "3~login~alreadylogged\n");
                        Client->running = 0;
                        return mess;
                    }
                    if (strncmp(Client->Server->users->array[i]->password, password, 19) == 0) {
                        Client->Server->users->array[i]->logged = 1;
                        Client->currentlyLogged = Client->Server->users->array[i];
                        Client->currentlyLogged->Client = Client;
                        strcpy(mess, "4~login~success~");
                        strcat(mess, Client->Server->users->array[i]->name);
                        strcat(mess, "\n");
                        return mess;
                    }
                    strcpy(mess, "3~login~failpassword\n");
                    Client->running = 0;
                    return mess;
                }
            }
            newUser = createUser(name, password, Client);
            newUser->logged = 1;
            addUser(Client->Server->users, newUser);
            Client->currentlyLogged = newUser;
            strcpy(mess, "4~login~registered~");
            strcat(mess, Client->Server->users->array[i]->name);
        }
    }else{
        printf("Client was null.\n");
    }
    strcat(mess, "\n");
    return mess;
}


char* getTables(struct client* Client){
    char num[10];
    static char mess[50];
    if(Client != NULL) {
        if(Client->currentlyLogged != NULL) {
            sprintf(num, "%d", Client->Server->numberOfTables);
            strcpy(mess, "3~tables~");
            strcat(mess, num);
        }
    }else{
        printf("Client was null.\n");
    }
    strcat(mess, "\n");
    return mess;
}

char* getTablePlaying(struct client* Client, int id){
    char num[20];
    static char mess[50];
    if(Client != NULL) {
        if(Client->currentlyLogged != NULL) {
            sprintf(num, "%d~%d", id, Client->Server->tables[id]->playingPos);
            strcpy(mess, "4~table~");
            strcat(mess, num);
        }
    }else{
        printf("Client was null.\n");
    }
    strcat(mess, "\n");
    return mess;
}

char* join(struct client* Client, int id){
    char idStr[10];
    char gameStr[10];
    static char mess[50];
    struct game* Game;
    if(Client != NULL) {
        if(Client->currentlyLogged != NULL) {
            Game = Client->Server->tables[id];
            sprintf(idStr, "%d", id);
            if (Game->playingPos == 5) {
                strcpy(mess, "4~join~");
                strcat(mess, idStr);
                strcat(mess, "~full\n");
                return mess;
            }
            if (Client->currentlyLogged->game != NULL && id != Client->currentlyLogged->game->id) {
                sprintf(gameStr, "%d", Client->currentlyLogged->game->id);
                strcpy(mess, "5~join~");
                strcat(mess, idStr);
                strcat(mess, "~alreadyplaying~");
                strcat(mess, gameStr);
                strcat(mess, "\n");
                return mess;
            }
            Client->currentlyLogged->game = Game;
            joinGameUser(Game, Client->currentlyLogged);
            notifyGameAboutJoin(Game, Client->currentlyLogged);
            strcpy(mess, "4~join~");
            strcat(mess, idStr);
            strcat(mess, "~success\n");
        }
    }else{
        printf("Client was null.\n");
    }
    return mess;
}

char* logout(struct client* Client){
    static char mess[50];
    if(Client != NULL) {
        if (Client->currentlyLogged != NULL) {
            Client->currentlyLogged->active = 0;
            notifyGameAboutLeave(Client->currentlyLogged->game, Client->currentlyLogged);
            Client->currentlyLogged->leaving = 1;
            Client->currentlyLogged->logged = 0;
            Client->currentlyLogged->Client = NULL;
            if(isGameFirstTurn(Client->currentlyLogged->game)){
                resetGame(Client->currentlyLogged->game);
            }
        }
        strcpy(mess, "3~logout~success\n");
    }else{
        printf("Client was null.\n");
    }
    return mess;
}

char* returnBack(struct client* Client){
    static char mess[50];
    if(Client != NULL) {
        if (Client->currentlyLogged != NULL) {
            Client->currentlyLogged->active = 0;
            notifyGameAboutLeave(Client->currentlyLogged->game, Client->currentlyLogged);
            Client->currentlyLogged->leaving = 1;
            if(isGameFirstTurn(Client->currentlyLogged->game)){
                resetGame(Client->currentlyLogged->game);
            }
        }
        strcpy(mess, "3~return~success\n");
    }else{
        printf("Client was null.\n");
    }
    return mess;
}

void getPlayers(struct client* Client){
    int i;
    char mess[50];
    if(Client != NULL) {
        if(Client->currentlyLogged->game != NULL) {
            for (i = 0; i < Client->currentlyLogged->game->playingPos; i++) {
                if (strcmp(Client->currentlyLogged->game->playing[i]->name, Client->currentlyLogged->name)) {
                    strcpy(mess, "3~playerJoined~");
                    strcat(mess, Client->currentlyLogged->game->playing[i]->name);
                    strcat(mess, "\n");
                    sendMessage(Client, mess);
                }
            }
        }
    }else{
        printf("Client was null.\n");
    }
}

void drawCard(struct client* Client){
    char num[10];
    char mess[50];
    if(Client != NULL) {
        if (Client->currentlyLogged != NULL && Client->currentlyLogged->game != NULL) {
            if (isGameFirstTurn(Client->currentlyLogged->game)) {
                Client->currentlyLogged->active = 1;
            }
            if (!Client->currentlyLogged->active) {
                sendMessage(Client, "3~draw~waiting\n");
            } else if (Client->currentlyLogged->hasEnough) {
                sendMessage(Client, "3~draw~haveEnough\n");
            } else if (isUserOver(Client->currentlyLogged)) {
                sendMessage(Client, "3~draw~areOver\n");
            } else if (Client->currentlyLogged->game->deckPos > 0) {
                sprintf(num, "%d", drawUserCard(Client->currentlyLogged));
                strcpy(mess, "4~draw~success~");
                strcat(mess, num);
                strcat(mess, "\n");
                sendMessage(Client, mess);
                notifyGameAboutDraw(Client->currentlyLogged->game, Client->currentlyLogged);
            }
            if (isUserOver(Client->currentlyLogged)) {
                fold(Client);
            }
            tryToEndGame(Client->currentlyLogged->game);
        } else {
            sendMessage(Client, "3~draw~invalidOperation\n");
        }
    }else{
        printf("Client was null.\n");
    }
}

void enough(struct client* Client){
    if(Client != NULL) {
        if(Client->currentlyLogged != NULL && Client->currentlyLogged->game != NULL) {
            if (!isUserOver(Client->currentlyLogged)) {
                userEnough(Client->currentlyLogged);
                sendMessage(Client, "3~enough~success\n");
                notifyGameAboutEnough(Client->currentlyLogged->game, Client->currentlyLogged);
            } else {
                sendMessage(Client, "3~enough~areOver\n");
            }
            tryToEndGame(Client->currentlyLogged->game);
        }
    }else{
        printf("Client was null.\n");
    }
}

void fold(struct client* Client){
    if(Client != NULL) {
        if(Client->currentlyLogged != NULL && Client->currentlyLogged->game != NULL) {
            if (isUserOver(Client->currentlyLogged)) {
                notifyGameAboutEnough(Client->currentlyLogged->game, Client->currentlyLogged);
            }
        }
    }else{
        printf("Client was null.\n");
    }
}

char* checkPlayers(struct client* Client){
    int i;
    int usrCnt = 0;
    static char mess[100];
    struct game* Game;
    if(Client != NULL) {
        if(Client->currentlyLogged != NULL && Client->currentlyLogged->game != NULL) {
            Game = Client->currentlyLogged->game;

            for (i = 0; i < Game->playingPos; ++i) {
                if (!memcmp(Game->playing[i]->Client->currentlyLogged->game, Game, sizeof(struct game))) {
                    usrCnt++;
                }
            }
            sprintf(mess, "%d", usrCnt + 2);
            strcat(mess, "~checkPlayers");


            for (i = 0; i < Game->playingPos; ++i) {
                if (!memcmp(Game->playing[i]->Client->currentlyLogged->game, Game, sizeof(struct game))) {
                    strcat(mess, "~");
                    strcat(mess, Game->playing[i]->name);
                }
            }
        }
        strcat(mess, "\n");
    }else{
        printf("Client was null.\n");
    }

    return mess;
}

void checkCards(struct client* Client){
    int i;
    static char mess[100];
    static char num[5];
    if(Client != NULL) {
        if(Client->currentlyLogged != NULL && Client->currentlyLogged->game != NULL) {
            sprintf(mess, "%d", Client->currentlyLogged->handPos + 2);
            strcat(mess, "~checkCards");
            for (i = 0; i < Client->currentlyLogged->handPos; i++) {
                strcat(mess, "~");
                sprintf(num, "%d", Client->currentlyLogged->hand[i]);
                strcat(mess, num);

            }

        }
        strcat(mess, "\n");
        sendMessage(Client, mess);
        if(Client->currentlyLogged->hasEnough){
            sendMessage(Client, "3~enough~success\n");
        }
        if(isUserOver(Client->currentlyLogged)){
            sendMessage(Client, "3~draw~areOver\n");
        }
    }else{
        printf("Client was null.\n");
    }

}