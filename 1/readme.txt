Klient i server byl nejdřív naprogramován celý v Javě.
Všechny soubory které jsou potřeba k buildu současného
serveru v C jsou umístěny ve složce c. 
Chyba:
Při připojení prvního uživatele se pointer na strukturu user uloží do pole.
Při připojení dalších uživatelů musí dojít ke kontrole tohoto pole kvůli 
duplicitě. V tomto poli po připojení druhého uživatele je však místo ukazatele
na prvního uživatele jen ukazatel na NULL. 
Jak vyvolat chybu:
Build serveru ze složky c
Spustit server (port 1234)
  Spustit 21.jar a 2x se pokusit přihlásit
  NEBO
  přes nc poslat packet ve tvaru 
  4~login~prihlasovacijemno~heslo
  také 2x
  {parita~akce~arg1~arg2}
