package com.redhat.demo.optaplanner.upstream;

import java.io.IOException;
import java.util.Properties;

import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.SaslQop;

public class HotRodClientConfiguration {

    private static final String HOTROD_PROPERTIES_FILE = "hotrod-client.properties";

    public static ConfigurationBuilder get() {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        Properties props = new Properties();
        try {
            props.load(HotRodClientConfiguration.class.getClassLoader().getResourceAsStream(HOTROD_PROPERTIES_FILE));
        } catch (IOException e) {
            throw new RuntimeException("Could not load infinispan.properties file.", e);
        }

        builder.addServer()
                .host(props.getProperty("infinispan.client.hotrod.endpoint"))
                .port(Integer.parseInt(props.getProperty("infinispan.client.hotrod.port")))
                .security().authentication()
                .enable()
                .username(props.getProperty("infinispan.client.hotrod.auth_username"))
                .password(props.getProperty("infinispan.client.hotrod.auth_password"))
                .realm(props.getProperty("infinispan.client.hotrod.auth_realm"))
                .serverName(props.getProperty("infinispan.client.hotrod.auth_server_name"))
                .saslMechanism(props.getProperty("infinispan.client.hotrod.sasl_mechanism"))
                .saslQop(SaslQop.valueOf(props.getProperty("infinispan.client.hotrod.sasl_properties.javax.security.sasl.qop")));
        return builder;
    }
}
