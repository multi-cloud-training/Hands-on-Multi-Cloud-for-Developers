package io.pivotal.demo.multideployment;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import java.util.Properties;

@SpringBootApplication
public class DemoApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.setProperty("isEmbedded", "true");

        new SpringApplicationBuilder(DemoApplication.class).properties(props).run(args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        Properties props = new Properties();
        props.setProperty("isEmbedded", "false");

        return application.sources(DemoApplication.class).properties(props);
    }
}