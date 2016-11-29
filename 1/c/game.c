#include "user.h"
#include "game.h"
#include <string.h>
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

void resetGameDeck(struct game* Game){
    int i;
    Game->deckPos = 32;
    for (i = 0; i < Game->deckPos; ++i) {
        Game->deck[i] = i;
    }
}

int isGameFirstTurn(struct game* Game){
    int i;
    for (i = 0; i < Game->playingPos; ++i) {
        if(Game->playing[i]->handPos != 0){
            return 0;
        }
    }
    return  1;
}

int drawGameCard(struct game* Game){
    if(isGameFirstTurn(Game)){
        resetGameDeck(Game);
    }
    if(Game->deckPos > 0){
        int card = rand() % Game->deckPos;
        int found = 0;
        int cardId = -1;
        int i;
        for (i = 0; i < Game->deckPos; ++i) {
            if(i == card){
                cardId = Game->deck[i];
                Game->deck[i] = -1;
                found = 1;
            }
            if(i == Game->deckPos-1){
                Game->deck[i] = -1;
                Game->deckPos--;
                break;
            }
            if(found){
                Game->deck[i] = Game->deck[i+1];
            }
        }
        return cardId;
    }else{
        return -1;
    }
}

void joinGameUser(struct game* Game, struct user* User){
    User->game = Game;
    Game->playing[Game->playingPos] = User;
    Game->playingPos++;
}

void kickGameUser(struct game* Game, struct user* User){
    int found = 0;
    int i;
    for (i = 0; i < Game->playingPos; ++i) {
        if(Game->playing[i]->name == User->name){
            Game->playing[i]->game = NULL;
            found = 1;
        }
        if(i == Game->playingPos -1){
            Game->playing[i] = NULL;
            Game->playingPos--;
            break;
        }
        if(found){
            Game->playing[i] = Game->playing[i+1];
        }
    }
}


void notifyGameAboutJoin(struct game* Game, struct user* User){
    int i;
    for (i = 0; i < Game->playingPos; i++) {
		/*	Game->playing[i]->ownThread.getOwnThread().out.println("3~playerJoined~"+joined.getName());*/
		}
}

/*
void notifyGameAboutDraw(struct game* Game, struct user* User){

}

void notifyGameAboutEnough(struct game* Game, struct user* User){

}

void notifyGameAboutFold(struct game* Game, struct user* User){

}
 */

void tryToEndGame(struct game* Game){
    int isEndable = 1;
    int i;
    for (i = 0; i < Game->playingPos; ++i) {
        if(!isUserOver(Game->playing[i]) && !Game->playing[i]->hasEnough){
            isEndable = 0;
        }
    }
    if(isEndable){
        endGame(Game);
    }
}

void endGame(struct game* Game){
    char* result;
    struct user* winners[5];
    int winnersCount = 0;
    int i;
    for (i = 0; i < Game->playingPos; i++) {
        if(Game->playing[i]->hasEnough && !isUserOver(Game->playing[i])){
            if(winnersCount==0){
                winners[winnersCount] = Game->playing[i];
                winnersCount++;
            }else{
                struct user* winner = winners[0];
                if(getUserHandValue(winner)<getUserHandValue(Game->playing[i])){
                    winnersCount = 1;
                    winners[0] = Game->playing[i];
                }else if(getUserHandValue(winner) == getUserHandValue(Game->playing[i])){
                    winners[winnersCount] = Game->playing[i];
                    winnersCount++;
                }

            }
        }
    }
    result = malloc(sizeof(char)*120);
    sprintf(result, "%d", 2+winnersCount);
    strcat(result, "~end");
    for (i = 0; i < winnersCount; i++) {
        strcat(result,"~");
        strcat(result,winners[i]->name);
    }
    strcat(result, "\n");
    /*
    notifyGameAboutWin(Game, result);
     */
}
/*
void notifyGameAboutWin(struct game* Game, char* winners);
 */