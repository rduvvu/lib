package com.jbhunt.edi.sterlingarchive.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OnErrorProcessor implements Processor {
    @Override
    public void process(Exchange exchange) {
        try {
            Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT,
                    Exception.class);

              log.error("RouteException: ",cause);
        } catch(Exception e) {
            log.error("There was an error processing an earlier exception.",e);
        }
    }
}
