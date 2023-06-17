package com.mikedll.headshot.model;

import java.io.IOException;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.HttpStatus;

public class RestErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse httpResponse) 
      throws IOException {
        return httpResponse.getStatusCode().isError();
    }

    @Override
    public void handleError(ClientHttpResponse httpResponse) 
      throws IOException {
        if (hasError(httpResponse)) {
            throw new RestClientException("Got " + httpResponse.getStatusCode().value() + " Status when executing REST Client request");
        }
    }
}
