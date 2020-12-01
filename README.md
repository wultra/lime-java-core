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

## Base Implementation of REST client

Class `DefaultRestClient` provides a base implementation of a REST client. The client provides an interface for calling HTTP methods: `GET`, `POST`, `PUT`, and `DELETE`.

The example below shows very basic initialization and usage of the REST client without any configuration:

```java
        DefaultRestClient restClient = new DefaultRestClient("http://localhost");
        ResponseEntity<String> response = restClient.get("/api/status", new ParameterizedTypeReference<String>() {});
```

### REST Client Configuration
In order to configure the REST client, you can use the builder interface:

```java
DefaultRestClient restClient = new DefaultRestClient.Builder("http://localhost").build();
```

The `baseUrl` parameter is mandatory and needs to be provided in the builder constructor. The following options are available for the builder:
- `contentType` - content type used for requests (default: `APPLICATION_JSON`)
- `acceptType` - accept type used for signalling the response type (default: `APPLICATION_JSON`)
- `proxy` - proxy settings (default: proxy is disabled)
  - `host` - proxy host
  - `port` - proxy port
  - `username` - proxy username
  - `password` - proxy password
- `connectionTimeout` - connection timeout in milliseconds (default: 5000 ms)
- `acceptInvalidSslCertificate` - whether invalid SSL certificate is accepted (default: false)
- `maxInMemorySize` - maximum in memory request size (default: 1048576 bytes)
- `httpBasicAuth` - HTTP basic authentication (default: disabled)
  - `username` - username for HTTP basic authentication
  - `password` - password for HTTP basic authentication

### Calling HTTP Methods Using REST Client

Once the rest client is initialized, you can use the following methods. Each method has two variants so that HTTP headers can be specified, if necessary.
- `get` - a blocking GET call with a generic response
- `getNonBlocking` - a non-blocking GET call with a generic response with `onSuccess` and `onError` consumers
- `getObject` - a blocking GET call with `ObjectResponse`
- `post` - a blocking POST call with a generic request / response
- `postNonBlocking` - a non-blocking POST call with a generic request / response with `onSuccess` and `onError` consumers
- `postObject` - a blocking POST call with `ObjectRequest` / `ObjectResponse`
- `put` - a blocking PUT call with a generic request / response
- `putNonBlocking` - a non-blocking PUT call with a generic request / response with `onSuccess` and `onError` consumers
- `putObject` - a blocking PUT call with `ObjectRequest` / `ObjectResponse`
- `delete` - a blocking DELETE call with a generic response
- `deleteNonBlocking` - a non-blocking DELETE call with a generic response with `onSuccess` and `onError` consumers
- `deleteObject` - a blocking DELETE call with `ObjectResponse`
