package org.mhc;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableMBeanExport;

@Configuration
@EnableMBeanExport
@ComponentScan(basePackages = "org.mhc")
public class JmxApp {
    public static void main(String[] args) {
        //-Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false  -Djava.rmi.server.hostname=127.0.0.1   -Dcom.sun.management.jmxremote.local.only=false   -Dcom.sun.management.jmxremote.port=9999
        AnnotationConfigApplicationContext applicationContext =
                new AnnotationConfigApplicationContext(JmxApp.class);
        Service service = applicationContext.getBean(Service.class);
        service.run();

    }
}
