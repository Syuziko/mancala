# Mancala Game API
The API consists of 3 endpoints. It is sufficient to play the game. The requests can be made via `http_client.http` which can be found in the project root, or by using the CURLs provided for each endpoint.

- ## Get Game
> ### GET /games/{game_id}
> Find a game by ID.
> Returns single game
#### Path parameters
> **game_id (required): UUID**
>
> ID of game to return; format: uuid
#### Return Type
> **Game:**
```json
{
    "gameId": "afe16479-63e8-4259-8846-7bb90e714cab",
    "players": [
        "First player",
        "Second player"
    ],
    "board": {
        "rows": [
            {
                "pits": [
                    6,
                    6,
                    6,
                    6,
                    6,
                    6,
                    0
                ]
            },
            {
                "pits": [
                    6,
                    6,
                    6,
                    6,
                    6,
                    6,
                    0
                ]
            }
        ]
    }
}
```
#### Response
> **200** - successful operation
>
> **404** - non-existent game for game_id value
>
> **500** - Unexpected errors
#### Curl
```bash
curl -X GET --location "http://localhost:8080/games/fea16ac2-bf4f-4f5d-bf9c-626eec0874da" \
    -H "Accept: application/json"
```

- ## Create game
> ### POST /games
> Creates a game for two players.
> Returns no content.
#### Request Body
> **body CreateGameRequest (required):**
>
> **player1** (required) \
> _type:_ string \
> _description_: First player name \
> _minLength_: 1 \
> _maxLength_: 50
> 
>
> **player2** (required) \
> _type:_ string \
> _description_: Second player name \
> _minLength_: 1 \
> _maxLength_: 50
```json
 {
    "player1": "First Player",
    "player2": "Second Player"
 
}
```
#### Return Type
> **CreateGameResponse** 
> 
> **game_id** \
> _type_: string \
> _format_: uuid \
> _description_: Newly created game id.

#### Response
> **201** - the game successfully created.
>
> **422** - invalid request data, i.e. too long player name.
>
> **500** - Unexpected errors
#### Curl
```bash
curl -X PUT --location "http://localhost:8080/games/fea16ac2-bf4f-4f5d-bf9c-626eec0874da" \
    -H "Content-Type: application/json" \
    -d "{
            \"player_index\": {{player_index}},
            \"pit_index\": {{pit_index}}
        }"
```

- ## Play game - sow pit stones
> ### PUT /games/{game_id}
> The player sows the stones.
> Returns no content.
#### Request Body
> **body PlayGameRequest (required):**
>
> **player_index** (required) \
> _type:_ integer \
> _description_: The player index. Possible values [0, 1] \
>
> **pit_index** (required) \
> _type:_ integer \
> _description_: Player's pit index. Possible values [0, 5] \
```json
 {
    "player_index": 0,
    "pit_index": 5
 
}
```
#### Return Type
> **No Content**

#### Response
> **204** - the pit stones have been successfully sowed.
>
> **422** - invalid request data, i.e. index out of possible bounds.
>
> **500** - Unexpected errors
> 
##### Curl
```bash
curl -X PUT --location "http://localhost:8080/games/fea16ac2-bf4f-4f5d-bf9c-626eec0874da" \
    -H "Content-Type: application/json" \
    -d "{
            \"player_index\": {{player_index}},
            \"pit_index\": {{pit_index}}
        }"
```


