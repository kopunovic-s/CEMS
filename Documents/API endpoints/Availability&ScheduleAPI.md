# Availability & Schedule API Documentation

## Overview

- There are 2 major sections to this API:
  - Availability
  - Schedule
- The availability API is used to set/get the availability of a user for a given day of the week
  - **Currently only users 12 (manager) and 25 (employee) have info set in them**
  - **End time cannot be 24:00. Use 23:59 instead.**
  - **The JSON body doesn't need all the days of the week, only the days that the user works i.e if the user works only Monday and Tuesday, then only include those 2 days in the JSON body.**
    - Both methods are fine though up to you for frontend implementation :).
- The schedule API is used to set/get the schedule of a user for a given day of the week
  - **date is in the format MM-dd-yyyy-HH:mm (24 hour format)**
  - **Schedules cannot be made if the user doesn't have availability set for that day**
  - **Currently only users 12 (manager), 14 (manager), and 25 (employee) have info set in them**
  - **Currently only the week of 4/6/2025 - 4/12/2025 has info set in them**
  - Idk anymore notes ask me questions I'm too lazy ts too much work

## Access Control

- OWNER/EXECUTIVE/MANAGER: Full access (view all, set availability/schedule)
- EMPLOYEE: View own availability/schedule. Edit own Availability.

## Endpoints

## Table of Contents

- [Availability Endpoints](#availability-endpoints)
  - [GET Requests](#get-requests)
  - [PUT Requests](#put-requests)
- [Schedule Endpoints](#schedule-endpoints)
  - [GET Requests](#get-requests)
  - [POST Requests](#post-requests)
  - [PUT Requests](#put-requests)

## Availability Endpoints

### GET Requests

#### Get a users availability

```
http://coms-3090-024.class.las.iastate.edu:8080/availability/{currentUser_id}/user/{targetUser_id}
```

- Example GET Availability Response

```json
[
  {
    "dayOfWeek": "MONDAY",
    "startTime": "09:00",
    "endTime": "24:00",
    "isAvailable": true
  },
  {
    "dayOfWeek": "TUESDAY",
    "startTime": "09:00",
    "endTime": "17:00",
    "isAvailable": true
  },
  {
    "dayOfWeek": "WEDNESDAY",
    "startTime": "09:00",
    "endTime": "17:00",
    "isAvailable": true
  },
  {
    "dayOfWeek": "THURSDAY",
    "startTime": "09:00",
    "endTime": "17:00",
    "isAvailable": true
  },
  {
    "dayOfWeek": "FRIDAY",
    "startTime": "09:00",
    "endTime": "17:00",
    "isAvailable": true
  }
]
```

### POST: Set a users availability

```
http://coms-3090-024.class.las.iastate.edu:8080/availability/{currentUser_id}/update/{targetUser_id}
```

- Example POST Availability Response (This response both updates and creates new availability)

```json
[
  {
    "dayOfWeek": "MONDAY",
    "startTime": "09:00",
    "endTime": "17:00",
    "isAvailable": true
  },
  {
    "dayOfWeek": "TUESDAY",
    "startTime": "09:00",
    "endTime": "17:00",
    "isAvailable": true
  },
  {
    "dayOfWeek": "WEDNESDAY",
    "startTime": "09:00",
    "endTime": "17:00",
    "isAvailable": false
  },
  {
    "dayOfWeek": "THURSDAY",
    "startTime": "09:00",
    "endTime": "17:00",
    "isAvailable": false
  }
]
```

## Schedule Endpoints

### GET Requests

##### Get a users day schedule

```
http://coms-3090-024.class.las.iastate.edu:8080/schedule/{currentUser_id}/user/{targetUser_id}/{date}
```

- Example Get Schedule Response for user 12 on 4/7/2025

```json
{
  "id": 1,
  "userId": 12,
  "startTime": "04-07-2025-09:00",
  "endTime": "04-07-2025-17:00"
}
```

#### Get all availabilities for a company

```
http://coms-3090-024.class.las.iastate.edu:8080/availability/{currentUser_id}/all
```

- Example Get All Availabilities Response

```json
[
    {
        "id": 2,
        "userId": 25,
        "dayOfWeek": "MONDAY",
        "startTime": "09:00",
        "endTime": "17:00",
        "isAvailable": true
    },
    {
        "id": 3,
        "userId": 25,
        "dayOfWeek": "TUESDAY",
        "startTime": "09:00",
        "endTime": "17:00",
        "isAvailable": true
    },
    More cards...
]
```

#### Get a users week schedules

```
http://coms-3090-024.class.las.iastate.edu:8080/schedule/{currentUser_id}/user/{targetUser_id}/week/{date}
```

- Example Get All Schedules Response

```json
[
  {
    "id": 1,
    "userId": 12,
    "startTime": "04-07-2025-09:00",
    "endTime": "04-07-2025-17:00"
  },
  {
    "id": 2,
    "userId": 12,
    "startTime": "04-08-2025-01:00",
    "endTime": "04-08-2025-17:00"
  },
  More cards...
  ]
```

#### Get all schedules for a day

```
http://coms-3090-024.class.las.iastate.edu:8080/schedule/{currentUser_id}/all/{date}
```

- Example Get All Schedules Response

```json
[
    {
        "id": 2,
        "userId": 12,
        "startTime": "04-08-2025-01:00",
        "endTime": "04-08-2025-17:00"
    },
    {
        "id": 5,
        "userId": 14,
        "startTime": "04-08-2025-10:00",
        "endTime": "04-08-2025-18:00"
    },
    {
        "id": 6,
        "userId": 25,
        "startTime": "04-08-2025-10:00",
        "endTime": "04-08-2025-18:00"
    },
    More cards...
]
```

#### Get all schedules for a week

```
http://coms-3090-024.class.las.iastate.edu:8080/schedule/{currentUser_id}/all/week/{date}
```

- Example Get All Schedules Response

```json
[
    {
        "id": 2,
        "userId": 12,
        "startTime": "04-08-2025-01:00",
        "endTime": "04-08-2025-17:00"
    },
    {
        "id": 5,
        "userId": 14,
        "startTime": "04-06-2025-10:00",
        "endTime": "04-06-2025-18:00"
    },
    {
        "id": 6,
        "userId": 25,
        "startTime": "04-09-2025-10:00",
        "endTime": "04-09-2025-18:00"
    },
    More cards...
]
```

### POST Requests

#### Create a week schedule for a user using their availabilities for the current week.

```
http://coms-3090-024.class.las.iastate.edu:8080/schedule/{currentUser_id}/createWeekAvailability/{targetUser_id}
```
```json
[]
```

#### Create a week schedule for a user using their availabilities on a certain week.

```
http://coms-3090-024.class.las.iastate.edu:8080/schedule/{currentUser_id}/createWeekAvailability/{targetUser_id}/{MM-dd-yyyy}
```
```json
[]
```

#### Create a single schedule for a user

```
http://coms-3090-024.class.las.iastate.edu:8080/schedule/{currentUser_id}/create/{targetUser_id}
```

- Example Create Schedule Response

```json
{
  "startTime": "04-07-2025-09:00",
  "endTime": "04-07-2025-17:00"
}
```

#### Create a week schedule for a user

```
http://coms-3090-024.class.las.iastate.edu:8080/schedule/{currentUser_id}/createWeek/{targetUser_id}
```

- Example Create Week Schedule Response

```json
[
  {
    "startTime": "04-07-2025-09:00",
    "endTime": "04-07-2025-17:00"
  },
  {
    "startTime": "04-08-2025-09:00",
    "endTime": "04-08-2025-17:00"
  },
  More cards...
]

```

#### Create a batch schedule for multiple users

```
http://coms-3090-024.class.las.iastate.edu:8080/schedule/{currentUser_id}/createBatch
```

- Example Create Batch Schedule Response

```json
[
    {
        "userId": 12,
        "startTime": "04-07-2025-09:00",
        "endTime": "04-07-2025-17:00"
    },
    {
        "userId": 12,
        "startTime": "04-08-2025-01:00",
        "endTime": "04-08-2025-17:00"
    },
    {
        "userId": 12,
        "startTime": "04-09-2025-10:00",
        "endTime": "04-09-2025-17:00"
    },
    {
        "userId": 12,
        "startTime": "04-10-2025-09:00",
        "endTime": "04-10-2025-17:00"
    },

    {
        "userId": 14,
        "startTime": "04-08-2025-10:00",
        "endTime": "04-08-2025-18:00"
    },
        {
        "userId": 25,
        "startTime": "04-08-2025-10:00",
        "endTime": "04-08-2025-18:00"
    },
    More cards...
]
```

### PUT Requests

##### Update schedules for multiple users (can be one too!) (**the id of the card is required in the JSON body**)

```
http://coms-3090-024.class.las.iastate.edu:8080/schedule/{currentUser_id}/updateBatch
```

- Example Update Schedule Response

```json
[
  {
    "id": 1,
    "userId": 12,
    "startTime": "04-07-2025-09:00",
    "endTime": "04-07-2025-17:00"
  },
  {
    "id": 2,
    "userId": 12,
    "startTime": "04-08-2025-01:00",
    "endTime": "04-08-2025-17:00"
  },
  {
    "id": 3,
    "userId": 12,
    "startTime": "04-09-2025-10:00",
    "endTime": "04-09-2025-17:00"
  },
  More cards...
]
```
