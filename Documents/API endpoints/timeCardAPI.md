# API Endpoints for TimeCard Object

## Table of Contents

- [Timecard Endpoints](#timecard-endpoints)
  - [IMPORTANT NOTES](#important-notes)
  - [GET Requests](#get-requests)
    - [Get All Timecards](#get-all-timecards)
    - [Get User's Timecards](#get-users-timecards)
    - [Get Latest Timecard](#get-latest-timecard)
  - [POST Requests](#post-requests)
    - [Clock In](#clock-in)
  - [PUT Requests](#put-requests)
    - [Clock Out](#clock-out)

## Timecard Endpoints

### IMPORTANT NOTES:

- A user can only have 1 timecard open at a time.
- If a user tries to clock in and has a timecard already open, the server will return a error.
- If a user clocks out, the timecard will be closed and the hours worked will be calculated.
- If a user tries to clock in again, a new timecard will be opened (if one doesn't already exist).
- If a user clocks out and has no timecard open, the server will return a error.

### GET Requests

##### Get All Timecards

```
http://coms-3090-024.class.las.iastate.edu:8080/Users/TimeCards
```

##### Get All Timecards for a User

```
http://coms-3090-024.class.las.iastate.edu:8080/Users/TimeCards/{userNumber}
```

##### Get Latest Timecard for a User

```
http://coms-3090-024.class.las.iastate.edu:8080/Users/TimeCards/latest/{userNumber}
```

##### Example Response:

```json
{
  "id": 1,
  "userNumber": 1,
  "clockIn": "2025-02-27T20:30:19",
  "clockOut": "2025-02-27T20:30:24",
  "hoursWorked": 0.0
}
```

### POST Requests

##### Clock In a User

```
http://coms-3090-024.class.las.iastate.edu:8080/Users/TimeCards/clockIn/{userNumber}
```

##### Example Post Response: Just use link to the endpoint (Backend AutoDeletes, no need to send JSON/objects)

### PUT Requests

##### Clock Out a User

```
http://coms-3090-024.class.las.iastate.edu:8080/Users/TimeCards/clockOut/{userNumber}
```

##### Example Put Response: Just use link to the endpoint (Backend AutoDeletes, no need to send JSON/objects)
