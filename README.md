# Wultra Java Core Libraries

This project contains Wultra Java Core classes that are shared across our projects.

## RESTful Model

All Wultra projects that use RESTful API to publish their services use common model structure. The core philosophy is following:

1) We use JSON format to transfer the data.
2) We always transfer an `object` as a request and response. Never an `array`, `string`, `decimal`, `boolean` or `null`.
3) The top most object is used for a "transport" information (paging, encryption, status, ...), the actual "business logic" information is embedded in request / response object attribute of the top-most object.

### Request

All requests have a following JSON structure:

```json
{
    "requestObject": {
        "_comment": "Request object attributes"
    }
}
```

To prepare a request with request object in Java, use:

```java
new ObjectRequest(someRequestObject);
```

### Success Response

For a simple OK response, we use following format:

```json
{
    "status": "OK"
}
```

To prepare a simple success response in Java, use:

```java
new Response();
```

### Success Response With Response Object

For an OK response with a response object, we use following format:

```json
{
    "status": "OK",
    "responseObject": {
        "_comment": "Request object attributes"
    }
}
```

Note that the response object may be a list of other objects, strings, decimals or booleans:

```json
{
    "status": "OK",
    "responseObject": [
        "_item1",
        "_item2"
    ]
}
```

To prepare a success response with a response object in Java, use:

```java
new ObjectResponse(someObjectResponse);
```

### Error Response

For an error response, we use following format that includes an error code and message for easier debugging:

```json
{
    "status": "ERROR",
    "responseObject": {
        "code": "SOME_ERROR_CODE",
        "message": "Some message, for debugging purposes"
    }
}
```

To prepare an error response with an error details in Java, use:

```java
new ErrorResponse("SOME_ERROR_CODE", "Some message, for debugging purposes");
```
