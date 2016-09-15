package com.example;

import org.springframework.cloud.client.loadbalancer.LoadBalancerRequest;
import org.springframework.cloud.netflix.ribbon.RibbonLoadBalancerClient;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;

import java.io.IOException;

public class RetryableRibbonLoadBalancerClient extends RibbonLoadBalancerClient {

    private  RetryTemplate retryTemplate;

    public RetryableRibbonLoadBalancerClient(SpringClientFactory clientFactory, RetryTemplate retryTemplate) {
        super(clientFactory);
        this.retryTemplate = retryTemplate;
    }

    @Override
    public <T> T execute(String serviceId, LoadBalancerRequest<T> request) throws IOException {
        return retryTemplate.execute(new RetryCallback<T, IOException>() {
            @Override
            public T doWithRetry(RetryContext context) throws IOException{
                return RetryableRibbonLoadBalancerClient.super.execute(serviceId, request);
            }
        });
    }
}
