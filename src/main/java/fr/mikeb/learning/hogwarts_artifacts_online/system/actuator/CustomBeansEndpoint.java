package fr.mikeb.learning.hogwarts_artifacts_online.system.actuator;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Endpoint(id = "custom-beans")
@Component
public class CustomBeansEndpoint {

    private final ApplicationContext applicationContext;

    public CustomBeansEndpoint(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @ReadOperation
    public int beanCount() {
        return applicationContext.getBeanDefinitionCount();
    }
}