# Mancala Game

An attempt to implement the game according to the Clean Architecture principles.

## Running it locally

> ### Prerequisites
> - Kotlin
> - JVM 17

### Via IntelliJ IDEA

- Open  the file `MancalaApplication.kt`.
- Click in the run icon from the main function and select `Modify Run Configuration...` option.

### Via Maven and terminal

- Open a terminal and execute the commands bellow

```bash
./mvnw install
./mvnw spring-boot:run
```

### Via Jar

- Open a terminal and execute the commands bellow
```bash
./mvnw package # or './mvnw package -DskipTests' to skip tests to be run.
java -jar target mancala-0.0.1-SNAPSHOT.jar
```

## API

Provides 3 endpoints to create game, update game (play game), and retrieve game by id. For more details see **API_README.md**

## How to play the game.

To play the game one should make API requests, no UI is provided. Below are the required steps to play the game.
> - Create a game by making a request to `create game` endpoint. Re-use returned game id for further requests.
> - First player makes a request to `update game`, i.e. sow pit stones. If the final stone ends up in the player's mancala(big pit), then second request to the same endpoint should be made by the same player.
You are the owner of your moves. If the final stone ends in the current player's own empty pit -> then opponents stones are automatically captured and placed in the current player's mancala.
> - Then, it is turn of the second player to sow pit stones. 
> - And so on, until the game ends : status == ENDED, or more precisely at least one row with small pits is empty. All stones are automatically collected and added to the corresponding mancala (big pit)
> - And remember, players like cheating. So be careful, when and who sows stones.