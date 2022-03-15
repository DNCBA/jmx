package org.mhc;

import com.alibaba.fastjson.JSON;
import com.sun.jmx.snmp.SnmpNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.TabularData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.*;

/**
 * Hello world!
 */
public class ConnectApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectApp.class);

    private static final String JMX_HOST = "jmxhost";
    private static final String JMX_PORT = "jmxport";

    public static void main(String[] args) {
        try {
            Properties config = loadConfig(args);
            connection(config);
        } catch (Exception e) {
            LOGGER.error("exception when connect jmx", e);
        }

    }

    private static void connection(Properties config) throws Exception {
        String jmxUrl = String.format("service:jmx:rmi:///jndi/rmi://%s:%s/jmxrmi", config.get(JMX_HOST), config.get(JMX_PORT));
        JMXServiceURL jmxServiceURL = new JMXServiceURL(jmxUrl);
        JMXConnector connect = JMXConnectorFactory.connect(jmxServiceURL);
        MBeanServerConnection mBeanServerConnection = connect.getMBeanServerConnection();
        String[] domains = mBeanServerConnection.getDomains();
        Integer mBeanCount = mBeanServerConnection.getMBeanCount();
        LOGGER.info("jmx domains: {}, mBeanCount:{}", domains, mBeanCount);

        Set<ObjectName> objectNames = mBeanServerConnection.queryNames(null, null);

        for (ObjectName objectName : objectNames) {
            MBeanInfo mBeanInfo = mBeanServerConnection.getMBeanInfo(objectName);
            MBeanAttributeInfo[] attributesArray = mBeanInfo.getAttributes();
            String[] attributes = new String[attributesArray.length];
            for (int i = 0; i < attributesArray.length; i++) {
                if ( attributesArray[i].isReadable()) {
                    attributes[i] = attributesArray[i].getName();
                    LOGGER.debug("{} find attributes  {}", objectName.getCanonicalName(), attributes[i]);
                }
            }
            AttributeList attributeList = mBeanServerConnection.getAttributes(objectName, attributes);
            for (Attribute attribute : attributeList.asList()) {
                Object value = attribute.getValue();
                String attributeName = attribute.getName();
                printData(objectName, attributeName, value);
            }
        }


        LOGGER.info("======================== success connection url: {} ==================", jmxUrl);
    }

    private static void printData(ObjectName objectName, String attribute, Object value) {
        if (value instanceof Number || value instanceof String || value instanceof Boolean) {
            LOGGER.info("{} getAttribute {}, {}", objectName.getCanonicalName(), attribute, value);
        } else if (value instanceof CompositeData) {
            CompositeData compositeData = (CompositeData) value;
            CompositeType type = compositeData.getCompositeType();
            for (String key : type.keySet()) {
                Object data = compositeData.get(key);
                printData(objectName, String.format("%s-%s", attribute, key), data);
            }
        } else if (value instanceof TabularData) {
            TabularData tabularData = (TabularData) value;
            Set<?> keySet = tabularData.keySet();
            Collection<?> values = tabularData.values();
            LOGGER.info("keySet : {} values : {}", JSON.toJSONString(keySet), JSON.toJSONString(values));
        } else {
            LOGGER.info("not support");
        }
    }

    private static Properties loadConfig(String[] args) throws IOException {
        Properties config = new Properties();
        ClassLoader classLoader = ConnectApp.class.getClassLoader();
        String defaultConfigFile = classLoader.getResource("config.properties").getFile();
        LOGGER.info("defaultConfigFile : {}", defaultConfigFile);
        config.load(classLoader.getResourceAsStream("config.properties"));
        if (null != args && args.length == 2) {
            String host = args[0];
            String port = args[1];
            config.put(JMX_HOST, host);
            config.put(JMX_PORT, port);
        }
        LOGGER.info("load properties config: {}", config);
        return config;
    }
}
