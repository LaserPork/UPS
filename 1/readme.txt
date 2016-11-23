Klient i server byl nejdřív naprogramován celý v Javě.
Všechny soubory které jsou potřeba k buildu současného
serveru v C jsou umístěny ve složce c. 
Chyba:
Při připojení prvního uživatele se pointer na strukturu user uloží do pole.
Při připojení dalších uživatelů musí dojít ke kontrole tohoto pole kvůli 
duplicitě. V tomto poli po připojení druhého uživatele je však místo ukazatele
na prvního uživatele jen ukazatel na NULL. 
