# Project API for API endpoints

## Table of Contents

- [Project Endpoints](#project-endpoints)
  - [IMPORTANT NOTES](#important-notes)
  - [GET Requests](#get-requests)
  - [POST Requests](#post-requests)
  - [PUT Requests](#put-requests)
  - [DELETE Requests](#delete-requests)

## Project Endpoints

### IMPORTANT NOTES:

- Projects can only be created based on a company ID.
- A project can only be closed if it is active.
- A project can only be reopened if it is closed.

### GET Requests

##### Get Project by ID (shows specific project)

```
http://coms-3090-024.class.las.iastate.edu:8080/Projects/id/{currentUser_id}/{project_id}
```

##### Get All projects by user ID (shows all projects for the company the user belongs to)

```
http://coms-3090-024.class.las.iastate.edu:8080/Projects/{currentUser_id}
```

##### Get All active projects by user ID

```
http://coms-3090-024.class.las.iastate.edu:8080/Projects/active/{currentUser_id}
```

##### Get All inactive projects by user ID

```
http://coms-3090-024.class.las.iastate.edu:8080/Projects/inactive/{currentUser_id}
```

##### Example Get Response

```json
{
  "id": 2,
  "name": "Test Project 2",
  "description": "2 This is a test project for ISU",
  "startDate": "2025-03-03T13:47:15",
  "closedDate": null,
  "active": true
}
```

### POST Requests

##### Create Project based on user ID (Create project based on the company the user belongs to)

```
http://coms-3090-024.class.las.iastate.edu:8080/Projects/{currentUser_id}
```

##### Example Post Response (only need 2 variables)

```json
{
  "name": "Test Project ",
  "description": "This is a test project for ISU"
}
```

### PUT Requests

##### Update Project based on Project ID & user ID

```
http://coms-3090-024.class.las.iastate.edu:8080/Projects/{currentUser_id}/{project_id}
```

```json
{
  "name": "Updated Test Project 2",
  "description": "Updated: This is a test project for ISU"
}
```

##### Close Project based on Project ID & user ID (Only URL, No JSON)

```
http://coms-3090-024.class.las.iastate.edu:8080/Projects/close/{currentUser_id}/{project_id}
```

##### Reopen Project based on Project ID & user ID (Only URL, No JSON)

```
http://coms-3090-024.class.las.iastate.edu:8080/Projects/open/{currentUser_id}/{project_id}
```

### DELETE Requests

##### Delete Project based on Project ID & user ID (Only URL, No JSON)

```
http://coms-3090-024.class.las.iastate.edu:8080/Projects/{currentUser_id}/{project_id}
```
