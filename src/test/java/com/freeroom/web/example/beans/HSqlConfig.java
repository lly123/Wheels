package com.freeroom.web.example.beans;

import com.freeroom.di.annotations.Bean;
import com.freeroom.persistence.DBConfig;

import java.util.Properties;

@Bean
public class HSqlConfig implements DBConfig
{
    @Override
    public Properties getDbProperties()
    {
        final Properties properties = new Properties();
        properties.setProperty("url", "jdbc:hsqldb:mem:bookdb");
        properties.setProperty("username", "sa");
        properties.setProperty("password", "");
        properties.setProperty("blockSize", "2");
        return properties;
    }
}
