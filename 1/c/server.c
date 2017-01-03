#include "server.h"
#include <stdlib.h>
#include <stdio.h>





struct server* createServer(int port, int numberOfTables){
    int i;
    struct server* Server;
    Server = malloc(sizeof(struct server));
    Server->numberOfTables = numberOfTables;
    Server->users = createUserArray();
    Server->threads = createClientArray();
    Server->tables = malloc(sizeof(struct game*) * numberOfTables);
    for (i = 0; i < numberOfTables; i++) {
        Server->tables[i] = createGame(i);

    }
    Server->port = port;
    return Server;
}

int runServer(struct server *Server){
    int sockfd, c_sockfd;
    struct sockaddr_in my_addr, rem_addr;
    int rem_addr_length;
    struct client* Client;
    int optval;

    if ((sockfd = socket(PF_INET, SOCK_STREAM, IPPROTO_TCP)) == -1) {
        perror("Socket nelze otevrit");
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
        rem_addr_length=sizeof(rem_addr);
        if ((c_sockfd = accept(sockfd, (struct sockaddr *)&rem_addr, &rem_addr_length)) == -1) {
            perror("Chyba pri accept\n");
            printf("Chyba");
            close(sockfd); return 1;
        }

        Client = createClient(Server, c_sockfd);
        addClient(Server->threads, Client);

    }
    return 0;

}

