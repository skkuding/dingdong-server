package org.skkuding.dingdongserver;

import org.skkuding.dingdongserver.auth.config.AuthProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackageClasses = AuthProperties.class)
public class DingdongServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DingdongServerApplication.class, args);
    }

}
