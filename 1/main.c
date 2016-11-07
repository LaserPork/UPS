#include <stdio.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <strings.h>
#include <sys/types.h>
#include <unistd.h>
#include <errno.h>

#define		PORT		1234
#define		BUFFSIZE	1000

pid_t spust(char* command) {
   pid_t pID = fork();
   if (pID == 0)
      execl(command, (char *) 0);
   return pID;
}


int main(int argc, char *argv[]) {
	char buf[BUFFSIZE];
	int sockfd, c_sockfd;
	struct sockaddr_in my_addr, rem_addr;
	int rem_addr_length;
	int prectenoDelka;

	char   buf_out[BUFFSIZE];
	int buf_len;
	int vysledek;

	if ((sockfd = socket(PF_INET, SOCK_STREAM, IPPROTO_TCP)) == -1) {
		perror("Socket nelze otevrit");
		exit(1);
	}

	bzero(&my_addr, sizeof(my_addr));
	my_addr.sin_family = AF_INET;
	my_addr.sin_port = htons(PORT);

	if (bind(sockfd, (struct sockaddr *)&my_addr, sizeof(my_addr)) == -1) {
		perror("Chyba v bind");
		close(sockfd); exit(1);
	}

	if (listen(sockfd, 5) == -1) {
		perror("Nelze provest listen");
		close(sockfd); exit(1);
	}

	printf("server bezi ...\n\n");
	while (1) {
		rem_addr_length=sizeof(rem_addr);
		if ((c_sockfd = accept(sockfd, (struct sockaddr *)&rem_addr, &rem_addr_length)) == -1) {
			perror("Chyba pri accept");
			close(sockfd); exit(1);
		}
		
		bzero(&buf, sizeof(buf));
		if ((prectenoDelka = recv(c_sockfd, buf, BUFFSIZE, 0)) == -1)
			perror("Chyba pri cteni");
		else {
			printf("prijat prikaz: %s \n", buf);                
			
			vysledek = spust(buf);
			if (vysledek<0) {
				sprintf(buf_out, "chyba");
				buf_len = 5;
			}
			else {
				sprintf(buf_out, "ok");
				buf_len = 2;
			}
			
			if (send(c_sockfd, buf_out, buf_len, 0) == -1) {
				perror("Chyba pri zapisu");
				break;
			}
		}
		close(c_sockfd);
	}
}
