package com.redhat.demo.optaplanner.upstream;

import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.SaslQop;

public class HotRodClientConfiguration {

    // This should match the value specified for the APPLICATION_NAME parameter when creating the datagrid-service
    private static final String HOTROD_SERVICE_NAME = "datagrid-service";

    private static final String DATAGRID_SERVICE_PROJECT_NAME = "datagrid-demo";

    // Hot Rod endpoint is the name of the exposed service combined with the service's project name
    // If the client is deployed in the same project as the service, then the hotrod service name is sufficient
//    private static final String HOT_ROD_ENDPOINT = "localhost";
    private static final String HOT_ROD_ENDPOINT =
            String.format("%s.%s.svc.cluster.local", HOTROD_SERVICE_NAME, DATAGRID_SERVICE_PROJECT_NAME);

    // This should match the value specified for the APPLICATION_USER parameter when creating the datagrid-service
    private static final String USERNAME = "admin";

    // This should match the value specified for the APPLICATION_USER_PASSWORD parameter when creating the datagrid-service
    private static final String PASSWORD = "admin";

    static ConfigurationBuilder get() {
//        Properties props = new Properties();
//        String infinispanHost;
//        int hotRodPort;
//        try {
//            props.load(HotRodClientConfiguration.class.getClassLoader().getResourceAsStream("infinispan.properties"));
//            infinispanHost = props.getProperty("infinispan.host");
//            hotRodPort = Integer.parseInt(props.getProperty("infinispan.hotrod.port"));
//        } catch (IOException e) {
//            e.printStackTrace();
//            throw new IllegalStateException("Could not load infinispan.properties file.");
//        }
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.addServer()
                .host(HOT_ROD_ENDPOINT)
                .port(11222)
                .security().authentication()
                .enable()
                .username(USERNAME)
                .password(PASSWORD)
                .realm("ApplicationRealm")
                .serverName(HOTROD_SERVICE_NAME)
                .saslMechanism("DIGEST-MD5")
                .saslQop(SaslQop.AUTH);
        return builder;
    }
}
