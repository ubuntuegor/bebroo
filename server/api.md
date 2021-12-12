# Bebroo API V1

Use query parameters for GET requests, `x-www-form-urlencoded` for POST and PATCH request bodies, and JSON for response
bodies.

## Errors

Server returns errors as plain text with 4xx response code. 401 probably means that your token is invalid. In case of an
unexpected server error 5xx code is returned with empty body.

## Authentication

`Authorization: Bearer {{jwt_token}}`

Token structure:

- `id`: `int` — user ID

## API Reference

### Google OAuth

#### Web
Open a popup navigating to `/api/auth/googleAuthorize`. After the login prompt, the token will be sent to the window that opened
the login popup ([reference](src/main/resources/templates/googleOAuthSuccess.ftl)).

#### Android
`POST /api/auth/googleIdToken`

- `idToken`: `String` - the id token from Google Sign-In

**Response**:

- `token`: `String`

### Authentication

`POST /api/auth/signup` — register a new user

- `username`: `String`
- `password`: `String`
- `displayName`: `String`

**Response**:

- `token`: `String`

---

`GET /api/auth/login` — login

- `username`: `String`
- `password`: `String`

**Response**:

- `token`: `String`

### User

`GET /api/user/me` — get info about current user  
*Requires authentication*

**Response**:

- `id`: `int`
- `displayName`: `String`
- `avatarUrl`: `String?`

---

`PATCH /api/user/me` — modify current user  
*Requires authentication*

- `displayName`: `String`

**Response**:
empty

### Boards

`GET /api/board/list` — get user's boards  
*Requires authentication*

**Response**:

- `Board[]`:
    - `uuid`: `String`
    - `name`: `String`
    - `isPublic`: `bool`
    - `lastOpened`: `Long` — timestamp
    - `creator`: `User`:
        - `id`: `int`
        - `displayName`: `String`
        - `avatarUrl`: `String`

---

`GET /api/board/{uuid}` — open a board (unless another's private)

- `showContributors`: `bool?` — default: false
- `showFigures`: `bool?` — default: false

**Response**:

- `uuid`: `String`
- `name`: `String`
- `isPublic`: `bool`
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

`PATCH /api/board/{uuid}` — modify a board

- `name`: `String?`
- `isPublic`: `bool?`

**Response**: empty

---

`POST /api/board/create` — create a board  
*Requires authentication*

- `name`: `String`

**Response**:

- `uuid`: `String`

### Board WebSocket

`WS /api/board/{uuid}/websocket`

- `figureId`: `int?` — last received figure ID

Communication using JSON

**Message types**:

- New figures
    - `action`: `addFigures`
    - `figures`: `Figure[]`
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
