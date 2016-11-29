#include "dynArray.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "user.h"
#include "game.h"


struct dynUserArray* createUserArray(){
    struct dynUserArray* array;
    array = malloc(sizeof(struct dynUserArray));
    array->array = malloc(sizeof(struct user*)*8);
    array->arraySize = 8;
    array->arrayPos = 0;
    return array;
}

struct dynClientArray* createClientArray(){
    struct dynClientArray* array;
    array = malloc(sizeof(struct dynClientArray));
    array->array = malloc(sizeof(struct client*)*8);
    array->arraySize = 8;
    array->arrayPos = 0;
    return array;
}

void addUser(struct dynUserArray* array, struct user* user){
    struct user** a = array->array;
    if(array->arrayPos == array->arraySize){
        array->arraySize *= 2;
        array->array = malloc(sizeof(struct user*) * array->arraySize);
        memcpy(array->array, a, sizeof(struct user*) * array->arraySize/2);
        free(a);
    }
    array->array[array->arrayPos] = user;
    array->arrayPos++;
}

void addClient(struct dynClientArray* array, struct client* client){
    struct client** a = array->array;
    if(array->arrayPos == array->arraySize){
        array->arraySize *= 2;
        array->array = malloc(sizeof(struct client*) * array->arraySize);
        memcpy(array->array, a, sizeof(struct client*) * array->arraySize/2);
        free(a);
    }
    array->array[array->arrayPos] = client;
    array->arrayPos++;
}

void freeArray(char** array, int size){
    int i;
    for (i = 0; i < size; ++i) {
        free(array[i]);
    }
}

