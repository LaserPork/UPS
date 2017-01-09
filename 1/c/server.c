#include "server.h"
#include <stdlib.h>
#include <string.h>
#include <errno.h>




struct server* createServer(int port, int numberOfTables){
    int i;
    struct server* Server;
    if((Server = malloc(sizeof(struct server))) == NULL){
        printf("Oh dear, something went wrong! Unable to allocate memory for Server\n");
        return NULL;
    }
    Server->numberOfTables = numberOfTables;
    Server->users = createUserArray();
    Server->threads = createClientArray();

    if((Server->tables = malloc(sizeof(struct game*) * numberOfTables)) == NULL){
        printf("Oh dear, something went wrong! Unable to allocate memory for Games\n");
        free(Server);
        return NULL;
    }
    for (i = 0; i < numberOfTables; i++) {
        Server->tables[i] = createGame(i);

    }
    Server->port = port;
    if( (Server->log = fopen("ServerLog.txt", "w")) == NULL){
        printf("Oh dear, something went wrong with opening log file! %s\n", strerror(errno));
        free(Server->tables);
        free(Server);
        return NULL;
    }
    return Server;
}

int runServer(struct server *Server){
    int sockfd, c_sockfd;
    struct sockaddr_in my_addr, rem_addr;
    socklen_t rem_addr_length;
    int optval;

    if ((sockfd = socket(PF_INET, SOCK_STREAM, IPPROTO_TCP)) == -1) {
        printf("Oh dear, something went wrong with opening socket! %s\n", strerror(errno));
        return 1;
    }

    optval = 1;
    setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR, &optval,
               sizeof(optval));

    memset(&my_addr, 0,sizeof(my_addr));
    my_addr.sin_family = AF_INET;
    my_addr.sin_port = htons(Server->port);

    if (bind(sockfd, (struct sockaddr *)&my_addr, sizeof(my_addr)) == -1) {
        perror("Chyba v bind");
        close(sockfd); return 1;
    }

    if (listen(sockfd, 5) == -1) {
        perror("Nelze provest listen");
        close(sockfd); return 1;
    }

    while (1) {
        printf("Server is listening for connections\n");
        fprintf(Server->log, "Server is listening for connections\n");
        fflush(Server->log);
        rem_addr_length=sizeof(rem_addr);
        if ((c_sockfd = accept(sockfd, (struct sockaddr *)&rem_addr, &rem_addr_length)) == -1) {
            perror("Chyba pri accept\n");
            printf("Chyba");
            fprintf(Server->log,"Chyba");
                    close(sockfd); return 1;
        }

        createClient(Server, c_sockfd);
        /*
        addClient(Server->threads, Client);
        */
    }
    return 0;

}

