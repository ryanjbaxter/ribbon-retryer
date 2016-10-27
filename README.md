# Ribbon Retryer

In Spring Cloud Brixton we do not use the Ribbon `RestClient` to make service requests so
you cannot rely on the retry functionality present in that class to reattempt failed requests.

One solution to this problem is to use [Spring Retry](https://github.com/spring-projects/spring-retry) 
in conjunction with Ribbon to add your own retry logic to your applications using Ribbon.

**Warning:**  The above statements only apply when using Ribbon directly
in Spring Cloud Brixton (all releases), Camden.RELEASE, and Camden.SR1.  If you are using a version
of Spring Cloud that is newer than Camden.SR1 the Spring Retry functionality is part of Spring Cloud
itself.  Also, if you are using 
Ribbon in conjunction with Zuul or Feign in Brixton or Camden different retry logic applies.

# Usage

Run the app by executing `./mvnw spring-boot:run`.  Hit http://localhost:8080 and
you should see the same content as if you went to http://example.com.

Lets take a look at the code executed when you hit the root of the webapp.
Most of the heavy lifting happens in `RetryerService`.
 
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
    listOfServers: example.com,retryableserver.com
```

As you can see, `service1` is configured to call either `example.com` or
`retryableserver.com`.  Since the Ribbon load balancer will use round robin load
balancing, when `getContent` is called it will rotate back and forth between the
two URLs.

If you try an hit `retryableserver.com` in your browser you will notice you get an error due
to the fact `retryableserver.com` is not a registered DNS address.  However in our app we 
always get the content from `example.com`, why is that?

The reason this works is because we have made some enhancements to the Ribbon load
balancer used by Spring Cloud.  If you look at the class `RetryableRibbonLoadBalancerCliet`
you can see that we are using the `RetryTemplate` from Spring Retry to wrap our call to the `execute` method.
When Ribbon selects `retryableserver.com` as the URL to use for our request, the request
request will fail but Spring Retry will retry the request again and due to the round robin
load balancing in Ribbon the next request will succeed.
