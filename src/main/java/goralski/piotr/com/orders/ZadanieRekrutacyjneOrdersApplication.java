package goralski.piotr.com.orders;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
@ConfigurationPropertiesScan
public class ZadanieRekrutacyjneOrdersApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZadanieRekrutacyjneOrdersApplication.class, args);
    }

}
