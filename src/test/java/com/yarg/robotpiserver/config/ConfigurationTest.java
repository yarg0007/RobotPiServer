package com.yarg.robotpiserver.config;

import com.yarg.gen.models.ConfigurationModel;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class ConfigurationTest {

    @BeforeMethod(alwaysRun = true)
    @AfterMethod(alwaysRun = true)
    public void resetConfig() {
        Configuration.getInstance().reinitializeToDefault();
    }

    @Test
    public void parseDefaultConfig() {
        ConfigurationModel model = Configuration.getInstance().getConfigurationModel();
        assertThat(model.getServerIpAddress(), is(equalTo("robotpi.local")));
        assertThat(model.getServerPort(), is(equalTo(1234)));
        assertThat(model.getServerBackLogging(), is(equalTo(10)));
    }

    @Test
    public void parseOverrideConfig() throws Exception {
        Configuration.getInstance().reinitializeWithResourceConfig("/com/yarg/robotpiserver/config/customConfig.json");
        ConfigurationModel model = Configuration.getInstance().getConfigurationModel();
        assertThat(model.getServerIpAddress(), is(equalTo("192.168.0.1")));
        assertThat(model.getServerPort(), is(equalTo(9999)));
        assertThat(model.getServerBackLogging(), is(equalTo(1)));
    }

    @Test
    public void resetToDefaultConfig() throws Exception {
        Configuration.getInstance().reinitializeWithResourceConfig("/com/yarg/robotpiserver/config/customConfig.json");
        Configuration.getInstance().reinitializeToDefault();
        ConfigurationModel model = Configuration.getInstance().getConfigurationModel();
        assertThat(model.getServerIpAddress(), is(equalTo("robotpi.local")));
        assertThat(model.getServerPort(), is(equalTo(1234)));
        assertThat(model.getServerBackLogging(), is(equalTo(10)));
    }
}