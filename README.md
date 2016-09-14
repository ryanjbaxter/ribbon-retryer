# Ribbon Retryer

In Spring Cloud Brixton we do not use the Ribbon `RestClient` to make service requests so
you cannot rely on the retry functionality present in that class to reattempt failed requests.

One solution to this problem is to use [Spring Retry](https://github.com/spring-projects/spring-retry) 
in conjunction with Ribbon to add your own retry logic to your applications using Ribbon.

**Warning:**  The above statements only apply when using Ribbon directly
in Spring Cloud Brixton.  If you are using 
Ribbon in conjustion with Zuul or Feign different retry logic applies.

# Usage

Run the app by executing `./mvnw spring-boot:run`.  Hit http://localhost:8080.
Every other time you hit this URL you will get an error page.  To understand why
 this is happening you can take a look at `RetryerService.getContent` method.
 
```
@Service
class RetryerService {

	private RestTemplate rest;

	public RetryerService(RestTemplate rest) {
		this.rest = rest;
	}

	public String getContent() {
		return rest.getForObject("http://service1/", String.class);
	}
}
```
The `RestTemplate` bean being injected into our `RetryerService` is "[load
balanced](http://cloud.spring.io/spring-cloud-static/spring-cloud.html#_spring_resttemplate_as_a_load_balancer_client)" 
meaning it is using the Ribbon load balancer.  The `getContent` method is making a
request to `service1`.  This service is configured in `application.yml`.

```
service1:
  ribbon:
    listOfServers: example.com,service1.com
```

As you can see, `service1` is configured to call either `example.com` or
`service1.com`.  Since the Ribbon load balancer will use round robin load
balancing, when `getContent` is called it will rotate back and forth between the
two URLs.

Since `service1.com` does not exist, requests to that URL will fail, this
is why every other request to http://localhost:8080 fails.

To fix this problem in our simple sample we can retry the request on failure.
This is where Spring Retry comes in.  If you hit http://localhost:8080/retry
you will notice you never get an error page.  This is because the `/retry`
endpoint calls `getContent` but is annotated with `@Retryable`, meaning if an
error occurs the method will be called again.  So when we get an error we will
make a second call which will succeed (due to the round robin load balancing from
Ribbon).