package com.redhat.demo.optaplanner.upstream;

import java.io.IOException;
import java.util.Properties;

import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.SaslQop;

public class HotRodClientConfiguration {

    private static final String HOTROD_PROPERTIES_FILE = "hotrod-client.properties";

    public static ConfigurationBuilder get() {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.addServer()
                .host(hotRodProperties("infinispan.client.hotrod.endpoint"))
                .port(Integer.parseInt(hotRodProperties("infinispan.client.hotrod.port")))
                .security().authentication()
                .enable()
                .username(hotRodProperties("infinispan.client.hotrod.auth_username"))
                .password(hotRodProperties("infinispan.client.hotrod.auth_password"))
                .realm(hotRodProperties("infinispan.client.hotrod.auth_realm"))
                .serverName(hotRodProperties("infinispan.client.hotrod.auth_server_name"))
                .saslMechanism(hotRodProperties("infinispan.client.hotrod.sasl_mechanism"))
                .saslQop(SaslQop.valueOf(hotRodProperties("infinispan.client.hotrod.sasl_properties.javax.security.sasl.qop")));
        return builder;
    }

    private static String hotRodProperties(String key) {
        Properties props = new Properties();
        try {
            props.load(HotRodClientConfiguration.class.getClassLoader().getResourceAsStream(HOTROD_PROPERTIES_FILE));
        } catch (IOException e) {
            throw new RuntimeException("Could not load infinispan.properties file.", e);
        }
        return props.getProperty(key);
    }
}
