cmake_minimum_required(VERSION 3.6)
project(1)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")
set(CMAKE_C_FLAGS "-ansi -pedantic -Wall -Wextra")

set(SOURCE_FILES
        c/main.c
        c/dynArray.c c/dynArray.h c/user.c c/user.h c/game.c c/game.h c/server.c c/server.h c/client.c c/client.h)

add_executable(1 ${SOURCE_FILES} c/dynArray.c c/dynArray.h c/user.c c/user.h c/game.c c/game.h c/server.c c/server.h)