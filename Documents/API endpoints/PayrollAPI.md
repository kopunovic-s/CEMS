# Payroll System API Documentation

## Overview

- The Payroll system automatically calculates pay based on employee timecards and hourly rates
- Payroll records are generated per week (Sunday-Saturday)
- Unpaid weeks auto-update with latest timecard data
- Only OWNER/EXECUTIVE roles can mark payroll status
- **Currently, week 12 & 13 have partial info in them: 3/16/25 - 3/22/25 & 3/23/25 - 3/29/25 respectively**

## Access Control

- OWNER/EXECUTIVE: Full access (view all, mark paid/unpaid)
- MANAGER: View access
- EMPLOYEE: View own records only

## Endpoints

## Table of Contents

- [Payroll Endpoints](#payroll-endpoints)
  - [Useful Postman Endpoints](#useful-postman-endpoints)
  - [GET Requests](#get-requests)
  - [PUT Requests](#put-requests)

## Useful Postman Endpoints

### PUT: Set a users timecard

```
http://coms-3090-024.class.las.iastate.edu:8080/users/timecards/setDay/{userNumber}
```

##### Example PUT Timecard Response

```json
{
  "clockIn": "2025-03-24T09:00:00",
  "clockOut": "2025-03-24T17:00:00"
}
```

### PUT: Set a users holy wage (no Json, only Link)

```
http://coms-3090-024.class.las.iastate.edu:8080/user/{currentUser_id}/updateWage/{targetUser_id}/{newWage}
```

## Payroll Endpoints

### GET Requests

##### Get weekly summary

```
http://coms-3090-024.class.las.iastate.edu:8080/payroll/{currentUser_id}/weekSummary/{date in format YYYY-MM-DD}
```

##### Example Get Response

```json
{
    "weekNumber": 13,
    "year": 2025,
    "processedDate": "2025-04-02T23:52:58.0670572",
    "isPaid": false,
    "employees": [
        {
            "user": {
                "firstName": "Miray",
                "lastName": "Hirabayashi",
                "id": 25,
                "role": "EMPLOYEE"
            },
            "totalHours": 40.0,
            "hourlyRate": 20.0,
            "totalPay": 800.0
        },
        {
            "user": {
                "firstName": "NewTestPerson",
                "lastName": "TestLastName",
                "id": 32,
                "role": "MANAGER"
            },
            "totalHours": 40.0,
            "hourlyRate": null,
            "totalPay": 0.0
        }
        more employees...
    ]
}

```

#### Get employee's weekly summary/details

```
http://coms-3090-024.class.las.iastate.edu:8080/payroll/{currentUser_id}/employeeSummary/{employeeId to view}/{date in format YYYY-MM-DD}
```

##### Example Get Response

```json
{
  "name": "Miray Hirabayashi",
  "role": "EMPLOYEE",
  "dailyDetails": [
    {
      "weekDay": "2025-03-24",
      "hoursWorked": 8.0,
      "times": "9:00 AM - 5:00 PM"
    },
    {
      "weekDay": "2025-03-25",
      "hoursWorked": 8.0,
      "times": "9:00 AM - 5:00 PM"
    },
    {
      "weekDay": "2025-03-26",
      "hoursWorked": 8.0,
      "times": "9:00 AM - 5:00 PM"
    },
    {
      "weekDay": "2025-03-27",
      "hoursWorked": 8.0,
      "times": "9:00 AM - 5:00 PM"
    },
    {
      "weekDay": "2025-03-28",
      "hoursWorked": 8.0,
      "times": "9:00 AM - 5:00 PM"
    }
  ]
}
```

### PUT Requests

##### Mark week as Paid (no Json, only Link)

```
http://coms-3090-024.class.las.iastate.edu:8080/payroll/{currentUser_id}/markPaid/{date in format YYYY-MM-DD}
```

#### Mark week as Unpaid (no Json, only Link)

```
http://coms-3090-024.class.las.iastate.edu:8080/payroll/{currentUser_id}/markUnpaid/{date in format YYYY-MM-DD}
```
