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
        RestClient restClient = new DefaultRestClient("http://localhost");
        ResponseEntity<String> responseEntity = restClient.get("/api/status", new ParameterizedTypeReference<String>() {});
        String response = responseEntity.getBody();
        HttpHeaders headers = responseEntity.getHeaders();
```

### REST Client Configuration
In order to configure the REST client, you can use the builder interface:

```java
RestClient restClient = DefaultRestClient.builder().baseUrl("http://localhost").build();
```

The following options are available for the builder:

- `baseUrl` - base URL for all requests, full URL is expected in request path if baseUrl is not specified
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
- `certificateAuth` - certificate authentication (default: disabled)
  - `useCustomKeyStore` - whether custom keystore should be used for certificate authentication (default: false)
  - `keyStoreLocation` - resource location of keystore (e.g. `file:/path_to_keystore`)
  - `keyStorePassword` - keystore password
  - `keyAlias` - key alias for the private key stored inside the keystore
  - `keyPassword` - password for the private key stored inside the keystore
  - `useCustomTrustStore` - whether custom truststore should be used for certificate authentication (default: false)
  - `trustStoreLocation` - resource location of truststore (e.g. `file:/path_to_truststore`)
  - `trustStorePassword` - truststore password
- `objectMapper` - custom object mapper for JSON serialization
- `filter` - custom `ExchangeFilterFunction` for applying a filter during communication
- `defaultHttpHeaders` - custom `HttpHeaders` to be added to all requests as default HTTP headers

### Calling HTTP Methods Using REST Client

Once the rest client is initialized, you can use the following methods. Each method has two variants so that HTTP headers can be specified, if necessary. The following methods are available:

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

The `path` parameter specified in requests can be either:

- a partial request path, in this case the `baseUrl` parameter must be configured during initialization
- a full URL, in this case the `baseUrl` parameter must not be configured during initialization

### Sample Usage

The example below shows how to use the Rest Client with `ObjectRequest` / `ObjectResponse` classes.

```java
        RestClient restClient = DefaultRestClient.builder()
            .baseUrl("http://localhost:8080/my-app")
            .build();

        // The requestData object contains data object which is serialized and sent to the server
        RequestData requestData = new RequestData(...);
        ObjectRequest<RequestData> objectRequest = new ObjectRequest<RequestData>(requestData);
        try {
            ObjectResponse<ResponseData> objectResponse = restClient.postObject("/api/endpoint", objectRequest, ResponseData.class);
            // The responseData object contains deserialized response received from the server
            ResponseData responseData = objectResponse.getResponseObject();
        } catch (RestClientException ex) {
            if (ex.getStatusCode() == HttpStatus.BAD_REQUEST) {
                // handle BAD_REQUEST error
            }
            ...
        }
```

### Error Handling

In case any HTTP error occurs during a blocking HTTP request execution, a `RestClientException` is thrown with following details:

- `statusCode` - an HTTP status code
- `response` - a raw error response
- `responseHeaders` - response HTTP headers
- `errorResponse` - a parsed `ErrorResponse`, only used for the `ObjectResponse` response type

Non-blocking methods provide an `onError` consumer for custom error handling.

## Wultra Auditing Library

The `audit-base` project provides auditing functionality for easier investigation of issues. Audit records are stored in a database and can be easily queried. The auditing library also handles removal of old audit records.

The audit library requires two database tables. The DDL is available for the following databases:
- [DDL for MySQL](./docs/sql/mysql/create_schema.sql)
- [DDL for Oracle](./docs/sql/oracle/create_schema.sql)
- [DDL for PostgreSQL](./docs/sql/postgresql/create_schema.sql)

### Configuration

The following configuration is required for integration of the auditing library:
- Enable scheduling on the application using `@EnableScheduling` annotation on class annotated with `@SpringBootApplication` so that the `flush` and `cleanup` functionality can be scheduled.
- Add the `com.wultra.core.audit.base` package to the `@ComponentScan`, e.g. `@ComponentScan(basePackages = {"...", "com.wultra.core.audit.base"})`, so that the annotations used in auditing library can be discovered.
- Configure the `spring.application.name` property to enable storing application name with audit records.

The following properties can be configured in case the default configuration needs to be changed:
- `audit.level` - minimum audit level (default: `INFO`)
- `audit.event.queue.size` - event queue size in memory (default: `100000`)
- `audit.storage.type` - storage type, reserved for future use (default: `DATABASE`)
- `audit.db.cleanup.days` - audit records older than specified number of days are deleted (default: `365`) 
- `audit.db.table.log.name` - name of audit log database table (default: `audit_log`)
- `audit.db.table.param.name` - name of audit parameters database table (default: `audit_param`)
- `audit.db.batch.size` - database batch size (default: `1000`)  

### Audit Levels

Following audit levels are available:
- `error` - an error occurred 
- `warn` - a minor error occurred
- `info` - informational message
- `debug` - debug message (disabled by default)
- `trace` - trace message (disabled by default)

### Code samples

Initialization of audit factory:
```java
@Configuration
@ComponentScan(basePackages = {"com.wultra.core.audit.base"})
public class WebServerConfiguration {

    private final AuditFactory auditFactory;

    @Autowired
    public WebServerConfiguration(AuditFactory auditFactory) {
        this.auditFactory = auditFactory;
    }

    @Bean
    public Audit audit() {
       return auditFactory.getAudit();
    }
}
```  

Autowiring:
```java
public class MyClass {
    private final Audit audit;

    @Autowired
    public MyClass(Audit audit) {
      this.audit = audit;
    }
}

```

Basic usage:
```java
   audit.info("a message");
```

Formatting messages:
```java
   audit.info("a message with {}", "formatting");
```

Auditing with specified level:
```java
   audit.log("a message for error level", AuditLevel.ERROR);
```

Auditing of exceptions:
```java
   audit.warn("a message", new Exception("an exception"));
```

Auditing with parameters:
```java
   audit.info("a message", AuditDetail.builder().param("my_id", "some_id").build());
```

Auditing with parameters and type of audit message:
```java
   String operationId = UUID.randomUUID().toString();
   Map<String, Object> param = new LinkedHashMap<>();
   param.put("user_id", "some_id");
   param.put("operation_id", operationId);
   audit.info("an access message", AuditDetail.builder().type("ACCESS").params(param).build());
```
