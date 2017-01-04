#include "user.h"
#include <stdlib.h>
#include <stdio.h>

struct game* createGame(int id){
    struct game* Game;
    Game = malloc(sizeof(struct game));
    Game->id = id;
    Game->playingPos = 0;
    Game->deckPos = 32;
    return Game;
}

void runGame(struct game* Game){
    pthread_create(&Game->tid, NULL, timer, Game);
    pthread_detach(Game->tid);
}

void* timer(void * voidGame){
    struct game* Game = (struct game*)voidGame;
    int i;
    sleep(20);
    pthread_setcancelstate(PTHREAD_CANCEL_DISABLE, NULL);
    for (i = 0; i < Game->playingPos; ++i) {
        if(Game->playing[i]->Client != NULL) {
            enough(Game->playing[i]->Client);
        }else{
            userEnough(Game->playing[i]);
            notifyGameAboutEnough(Game->playing[i]->game, Game->playing[i]);
        }

    }
    tryToEndGame(Game);
    return NULL;
}

void resetGameDeck(struct game* Game){
    int i;
    if(Game != NULL) {

        Game->deckPos = 32;
        for (i = 0; i < Game->deckPos; ++i) {
            Game->deck[i] = i;
        }
    }
}

int isGameFirstTurn(struct game* Game){
    int i;
    if(Game != NULL) {
        for (i = 0; i < Game->playingPos; ++i) {
            if (Game->playing[i]->handPos != 0) {
                return 0;
            }
        }

    }
    return 1;
}

int drawGameCard(struct game* Game){
    int card;
    int found;
    int cardId;
    int i;
    if(Game != NULL) {
        if (isGameFirstTurn(Game)) {
            resetGameDeck(Game);
            runGame(Game);
        }
        if (Game->deckPos > 0) {
            srand( (unsigned int)time(NULL) );
            card = rand() % Game->deckPos;
            found = 0;
            cardId = -1;
            for (i = 0; i < Game->deckPos; ++i) {
                if (i == card) {
                    cardId = Game->deck[i];
                    Game->deck[i] = -1;
                    found = 1;
                }
                if (i == Game->deckPos - 1) {
                    Game->deck[i] = -1;
                    Game->deckPos--;
                    break;
                }
                if (found) {
                    Game->deck[i] = Game->deck[i + 1];
                }
            }
            return cardId;
        } else {
            return -1;
        }
    }
    return -1;
}

void joinGameUser(struct game* Game, struct user* User){
    int i;
    int found = 0;
    if(Game != NULL && User != NULL) {
        for (i = 0; i < Game->playingPos; ++i) {
            if (strncmp(Game->playing[i]->name, User->name, 19) == 0) {
                found = 1;
                User->active = 1;
                notifyGameAboutComingBack(Game, User);
                break;
            }
        }
        User->game = Game;
        User->leaving = 0;
        if(!found) {
            Game->playing[Game->playingPos] = User;
            Game->playingPos++;
            if(isGameFirstTurn(Game)){
                User->active = 1;
            }
        }
    }
}

void kickGameUser(struct game* Game, struct user* User){
    int found = 0;
    int i;
    if(Game != NULL && User != NULL) {
        printf("In Table %d is %d players. Kicking %s \n", Game->id + 1, Game->playingPos, User->name);
        for (i = 0; i < Game->playingPos; ++i) {
            if (strncmp(Game->playing[i]->name, User->name, 19) == 0) {
                Game->playing[i]->game = NULL;
                found = 1;
                printf("Player %s kicked.\n", Game->playing[i]->name);
            }
            if (i == Game->playingPos - 1) {
                Game->playing[i] = NULL;
                Game->playingPos--;
                break;
            }
            if (found) {
                Game->playing[i] = Game->playing[i + 1];
            }
        }

        if (!found) {
            printf("Did not found\n");
        }
    }
}


void notifyGameAboutJoin(struct game* Game, struct user* User){
    int i;
    static char mess[50];
    if(Game != NULL && User != NULL) {
        strcpy(mess, "3~playerJoined~");
        strcat(mess, User->name);
        strcat(mess, "\n");
        for (i = 0; i < Game->playingPos; i++) {
            if(strcmp(Game->playing[i]->name, User->name)) {
                sendMessage(Game->playing[i]->Client, mess);
            }
        }
    }
}

void notifyGameAboutComingBack(struct game* Game, struct user* User){
    int i;
    char mess[50];
    if(Game != NULL && User != NULL) {
        strcpy(mess, "3~playerCameBack~");
        strcat(mess, User->name);
        strcat(mess, "\n");
        for (i = 0; i < Game->playingPos; i++) {
            if (strcmp(Game->playing[i]->name, User->name)) {
                sendMessage(Game->playing[i]->Client, mess);
            }
        }
    }
}

void notifyGameAboutDraw(struct game* Game, struct user* User){
    int i;
    char mess[50];
    char num[10];
    if(Game != NULL && User != NULL) {
        strcpy(mess, "4~playerDraw~");
        strcat(mess, User->name);
        strcat(mess, "~");
        sprintf(num, "%d", User->handPos);
        strcat(mess, num);
        strcat(mess, "\n");
        for (i = 0; i < Game->playingPos; i++) {
            if (strcmp(Game->playing[i]->name, User->name)) {
                sendMessage(Game->playing[i]->Client, mess);
            }
        }
    }
}

void notifyGameAboutEnough(struct game* Game, struct user* User){
    int i;
    char mess[50];
    if(User != NULL && Game != NULL) {
        strcpy(mess, "3~playerEnough~");
        strcat(mess, User->name);
        strcat(mess, "\n");
        for (i = 0; i < Game->playingPos; i++) {
            if (strcmp(Game->playing[i]->name, User->name)) {
                sendMessage(Game->playing[i]->Client, mess);
            }
        }
    }
}

void notifyGameAboutLeave(struct game* Game, struct user* User){
    int i;
    char mess[50];
    if(Game != NULL && User != NULL) {
        strcpy(mess, "3~playerLeft~");
        strcat(mess, User->name);
        strcat(mess, "\n");
        for (i = 0; i < Game->playingPos; i++) {
            sendMessage(Game->playing[i]->Client, mess);
        }
    }
}


void tryToEndGame(struct game* Game){
    int isEndable = 1;
    int i;
    if(Game != NULL) {
        for (i = 0; i < Game->playingPos; ++i) {
            if (!isUserOver(Game->playing[i]) && !Game->playing[i]->hasEnough && Game->playing[i]->active) {
                isEndable = 0;
            }
        }
        if (isEndable) {
            endGame(Game);
        }
    }
}

void endGame(struct game* Game){
    char* result;
    struct user* winners[5];
    int winnersCount = 0;
    int i;
    if(Game != NULL) {
        pthread_cancel(Game->tid);
        for (i = 0; i < Game->playingPos; i++) {
            if (Game->playing[i]->hasEnough && !isUserOver(Game->playing[i])) {
                if (winnersCount == 0) {
                    winners[winnersCount] = Game->playing[i];
                    winnersCount++;
                } else {
                    struct user *winner = winners[0];
                    if (getUserHandValue(winner) < getUserHandValue(Game->playing[i])) {
                        winnersCount = 1;
                        winners[0] = Game->playing[i];
                    } else if (getUserHandValue(winner) == getUserHandValue(Game->playing[i])) {
                        winners[winnersCount] = Game->playing[i];
                        winnersCount++;
                    }

                }
            }
        }
        result = malloc(sizeof(char) * 120);
        sprintf(result, "%d", 2 + winnersCount);
        strcat(result, "~end");
        for (i = 0; i < winnersCount; i++) {
            strcat(result, "~");
            strcat(result, winners[i]->name);
        }
        strcat(result, "\n");

        notifyGameAboutWin(Game, result);
    }
}

void notifyGameAboutWin(struct game* Game, char* winners){
    int i;
    if(Game != NULL && winners != NULL) {
        for (i = 0; i < Game->playingPos; i++) {
            sendMessage(Game->playing[i]->Client, winners);
        }
        sleep(3);
        resetGame(Game);
    }
}

void notifyGameAboutPlayers(struct game* Game){
    int i;
    if(Game != NULL) {
        for (i = 0; i < Game->playingPos; i++) {
            sendMessage(Game->playing[i]->Client, checkPlayers(Game->playing[i]->Client));
        }
    }
}

void resetGame(struct game* Game){
    int i;
    if(Game != NULL) {
        resetGameDeck(Game);
        for (i = 0; i < Game->playingPos; i++) {
            if(resetUser(Game->playing[i])){
                i--;
            }
        }
        notifyGameAboutPlayers(Game);
        for (i = 0; i < Game->playingPos; i++) {
            sendMessage(Game->playing[i]->Client, "2~reset\n");
        }
    }
}
