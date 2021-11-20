# Bebroo API V1

Use query parameters for GET requests, `x-www-form-urlencoded` for POST and PATCH request bodies, and JSON for response bodies. 

## Errors
Server returns errors as plain text with 5xx response code.

## Authentication
`Authorization: Bearer {{jwt_token}}`

Token structure:
- `id`: `int` — user ID

## API Reference
### User
`POST /api/users/signup` — register a new user
- `username`: `String`
- `password`: `String`
- `displayName`: `String`

**Response**:
- `token`: `String`

---

`GET /api/users/login` — login
- `username`: `String`
- `password`: `String`

**Response**:
- `token`: `String`

---

`GET /api/users/me` — get info about current user  
*Requires authorization*

**Response**:
- `id`: `int`
- `displayName`: `String`
- `avatarUrl`: `String?`

---

`PATCH /api/users/me` — modify current user  
*Requires authorization*
- `displayName`: `String`

**Response**:
empty

### Boards
`GET /api/boards/list` — get user's boards  
*Requires authorization*

**Response**:
- `Board[]`:
  - `uuid`: `String`
  - `name`: `String`
  - `lastOpened`: `int` — timestamp
  - `creator`: `User`:
    - `id`: `int`
    - `displayName`: `String`
    - `avatarUrl`: `String`

---

`GET /api/boards/open` — open a board (unless another's private)
- `uuid`: `String`
- `fields`: `String` — comma-separated list of returned fields: `name`, `private`, `creator`, `contributors`, `figures` (default: `name`)

**Response**:
- `uuid`: `String`
- `name`: `String`
- `private`: `bool`
- `creator`: `User`:
  - `id`: `int`
  - `displayName`: `String`
  - `avatarUrl`: `String`
- `contributors`: `User[]`:
  - `id`: `int`
  - `displayName`: `String`
  - `avatarUrl`: `String`
- `figures`: `Figure[]`:
  - `id`: `int`
  - `drawingData`: `String`
  - `color`: `String`
  - `strokeWidth`: `int`

---

`POST /api/boards/create` — create a board  
*Requires authentication*
- `name`: `String`

**Response**:
- `uuid`: `String`

---

`PATCH /api/boards/edit` — modify a board
- `uuid`: `String`
- `name`: `String?`
- `private`: `bool?`

**Response**: empty

### Board WebSocket

`WS /api/boards/websocket`
- `uuid`: `String`
- `figureId`: `int?` — last received figure ID

Communication using JSON

**Message types**:  
- New figure
  - `action`: `addFigure`
  - `figure`: `Figure`
- Remove figure
  - `action`: `removeFigure`
  - `figureId`: `int`
- List connected users - upon connection
  - `action`: `connectedUsers`
  - `users`: `User[]`
- User connected
  - `action`: `userConnected`
  - `user`: `User`
- User disconnected
  - `action`: `userDisconnected`
  - `userId`: `int`

**User to server** (*only allowed for authenticated users*): `addFigure`, `removeFigure`  
**Server to user**: `addFigure`, `removeFigure`, `connectedUsers`, `userConnected`, `userDisconnected`  