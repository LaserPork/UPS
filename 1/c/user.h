#ifndef INC_1_USER_H
#define INC_1_USER_H

#include <pthread.h>

struct user{
    char *name;
    char *password;
    struct game *game;
    int hand[32];
    int handPos;
    pthread_t *ownThread;

    int logged;
    int hasEnough;
};

struct user* createUser(char *name, char *password, pthread_t* ownThread);

void resetUser(struct user* User);

void userEnough(struct user* User);

int isUserOver(struct user* User);

void dropUserHand(struct user* User);

int drawUserCard(struct user* User);

int getUserHandValue(struct user* User);

int getCardValue(int cardId);



#endif
