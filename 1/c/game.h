#ifndef INC_1_GAME_H
#define INC_1_GAME_H

#include "user.h"

struct game{
    struct user* playing[5];
	int playingPos;
	int id;
	int deck[32];
	int deckPos;

};

struct game* createGame(int id);

void resetGameDeck(struct game* Game);

int isGameFirstTurn(struct game* Game);

int drawGameCard(struct game* Game);

void joinGameUser(struct game* Game, struct user* User);

void kickGameUser(struct game* Game, struct user* User);

void notifyGameAboutJoin(struct game* Game, struct user* User);

void notifyGameAboutDraw(struct game* Game, struct user* User);

void notifyGameAboutEnough(struct game* Game, struct user* User);

void notifyGameAboutFold(struct game* Game, struct user* User);

void tryToEndGame(struct game* Game);

void endGame(struct game* Game);

void notifyGameAboutWin(struct game* Game, char* winners);


#endif