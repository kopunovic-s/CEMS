# API Endpoints for User Profile

### Important Notes

- There are 2 parts, one for the user profile and one for the company profile
- Executives & higher can edit all fields of a user (img included)
- Users can only edit their own profile (img included)
- For the profile picture of each, import a file.png & file.jpg
  - They are returned as a base64 string for frontend to display
- Only Executives & Owners can edit the company profile

### GET User & Company Profile

#### Get User Profile

```
http://coms-3090-024.class.las.iastate.edu:8080/users/{currentUser_id}/{userToView_id}
```

##### Example Get User Profile Request (also gets images):

```
{
  "firstName": "string",
  "lastName": "string",
  "password": "string",
  "role": "OWNER",
  "email": "string",
  "hourlyRate": 0,
  "streetAddress": "string",
  "city": "string",
  "state": "string",
  "zipCode": "string",
  "country": "string",
  "ssn": "string",
  "profileImage": "string",
  "id": 0,
  "companyName": "string",
  "companyId": 0
}
```

#### Get Company Profile (also gets images):

```
http://coms-3090-024.class.las.iastate.edu:8080/Companies/{companyId}

```

```
{
  "id": 0,
  "name": "string",
  "ein": "string",
  "streetAddress": "string",
  "city": "string",
  "state": "string",
  "zipCode": "string",
  "country": "string",
  "companyLogo": "string"
}
```

### PUT Request for User & Company Profile

#### Notes

- The user & company profile doesn't need to have all the fields, just the ones you want to update
- Only Executives & above can edit wages of a user

#### PUT (edit user info):

```
http://coms-3090-024.class.las.iastate.edu:8080/users/{currentUser_id}/update-info/{targetUser_id}
```

##### Example PUT (edit user info) Request:

```
{
    "firstName": "Miray",
    "lastName": "H",
    "password": "password",
    "role": "EMPLOYEE",
    "email": "testEmail2@gmail.com",
    "hourlyRate": 30.0,
    "streetAddress": "567 fake address",
    "city": "Ames",
    "state": "IA",
    "zipCode": "50012",
    "country": "USA",
    "ssn": "123-45-611"
}
```

or (only edit these 3 fields)

```
{
    "lastName": "Hello",
    "password": "123",
    "zipCode": "50000",
}
```

#### PUT (edit company info):

```
http://coms-3090-024.class.las.iastate.edu:8080/companies/{currentUser_id}/{companyId}

```

##### Example PUT (edit company info) Request:

```
{
"companyName": "Hello",
"ein": "123456789",
}
```

#### PUT (edit company logo):

```
http://coms-3090-024.class.las.iastate.edu:8080/images/company/{currentUser_id}/{companyId}
```

##### Example PUT (edit company logo) Request:

```
img.png
```

#### PUT (edit user profile picture):

```
http://coms-3090-024.class.las.iastate.edu:8080/images/user/{currentUser_id}/{targetUser_id}
```

##### Example PUT (edit user profile picture) Request:

```
img.png
```

### Individual Img Request

##### GET (get user profile picture):

```
http://coms-3090-024.class.las.iastate.edu:8080/images/user/{currentUser_id}/{targetUser_id}
```

##### GET (get company logo):

```
http://coms-3090-024.class.las.iastate.edu:8080/images/company/{currentUser_id}/{companyId}
```

##### DELETE (delete user profile picture):

```
http://coms-3090-024.class.las.iastate.edu:8080/images/user/{currentUser_id}/{targetUser_id}
``` 

##### DELETE (delete company logo):

```
http://coms-3090-024.class.las.iastate.edu:8080/images/company/{currentUser_id}/{companyId}
```




