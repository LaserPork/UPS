#ifndef INC_1_DYNARRAY_H
#define INC_1_DYNARRAY_H

#include "user.h"
#include "game.h"
#include "client.h"

struct dynUserArray {
    struct user** array;
    int arraySize;
    int arrayPos;
};

struct dynClientArray {
    struct client** array;
    int arraySize;
    int arrayPos;
};

struct dynUserArray* createUserArray();

struct dynClientArray* createClientArray();

void addUser(struct dynUserArray *array, struct user *user);

void addClient(struct dynClientArray *array, struct client *client);

void freeArray(char** array, int size);

char *multi_tok(char *input, char *delimiter);

#endif
