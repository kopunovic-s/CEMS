# API Endpoints for W-2

### Important Notes

- only 2 main functions: GET info and GET pdf
  - GET info returns the W-2 info for a given employee and year in JSON format
  - GET pdf returns the pdf for a given employee and year
- Only Executives & Owners can access other users' W-2 info
- Employees and managers can only access their own W-2 info
- **PDF & JSON info auto updates when the GET request is made**
  - So if a user changes their address or puts in more hours, the info is updated when the GET request is made
- **There are checks in place that prevent a W-2 from being generated if the employee or company info is not complete**
  - Things each user needs to have: first name, last name, ssn, street address, city, state, zip code
  - Things each company needs to have: ein, street address, city, state, zip code
  - Everything else is optional and can be added later (mainly wages)

### GET pdf

```
http://coms-3090-024.class.las.iastate.edu:8080/w2/{currentUser_id}/{targetUser_id}/{year}/pdf
```

#### Example GET pdf Request:

#### [example_w2.pdf](./example_w2.pdf)

### GET info

```
http://coms-3090-024.class.las.iastate.edu:8080/w2/{currentUser_id}/{targetUser_id}/{year}
```

##### Example GET info Request (also gets images):

```
{
    "id": 8,
    "year": 2024,
    "employeeSsn": "123-45-611",
    "employeeFirstName": "Miray",
    "employeeLastName": "H",
    "employeeAddress": "567 fake address",
    "employeeCity": "Ames",
    "employeeState": "IA",
    "employeeZip": "50000",
    "employerEin": "12-3456789",
    "employerName": "Andy Chen",
    "employerAddress": "2505 Union Dr",
    "employerCity": "Ames",
    "employerState": "IA",
    "employerZip": "50011",
    "wagesTipsOtherComp": 71120.0,
    "federalIncomeTax": 15646.4,
    "socialSecurityWages": 71120.0,
    "socialSecurityTax": 4409.44,
    "medicareWages": 71120.0,
    "medicareTax": 1031.24,
    "stateCode": "IA",
    "stateId": "12-3456789",
    "stateWages": 71120.0,
    "stateIncomeTax": 3556.0
}
```
