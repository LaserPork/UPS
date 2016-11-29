#include "user.h"
#include "game.h"
#include <string.h>
#include <stdlib.h>



struct user* createUser(char *name, char *password, struct client* Client){
    struct user* User;
    User = malloc(sizeof(struct user));
    User->name = malloc(sizeof(char) * 20);
    User->password = malloc(sizeof(char) * 20);
    strcpy(User->name, name);
    strcpy(User->password, password);
    User->Client = Client;
    User->handPos = 0;
    User->logged = 0;
    User->hasEnough = 0;

    return User;
}

void resetUser(struct user* User){
    dropUserHand(User);
    User->hasEnough = 0;
}

void userEnough(struct user* User){
    User->hasEnough = 1;
}

int isUserOver(struct user* User){
    if(getUserHandValue(User)>21){
        return 1;
    }else{
        return 0;
    }
}

void dropUserHand(struct user* User){
    int i;
    for (i = 0; i < 32; ++i) {
        User->hand[i] = -1;
    }
    User->handPos = 0;
}

int drawUserCard(struct user* User){
    int card = drawGameCard(User->game);
    User->hand[User->handPos] = card;
    User->handPos++;
    return card;
}

int getUserHandValue(struct user* User){
    int value = 0;
    int i;
    for (i = 0; i < User->handPos; ++i) {
        value += getCardValue(User->hand[i]);
    }
    return value;
}

int getCardValue(int cardId){
    int sign = cardId%8;
    switch (sign) {
        case 0: return 7;
        case 1: return 8;
        case 2: return 9;
        case 3: return 10;
        case 4: return 1;
        case 5: return 1;
        case 6: return 2;
        case 7: return 11;
    }
    return -9999;
}

