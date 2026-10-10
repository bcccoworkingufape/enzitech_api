package br.edu.ufape.enzitech.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.boot.info.BuildProperties;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class SwaggerConfigTest {

    @Test
    void usesVersionFromBuildInfo() {
        Properties properties = new Properties();
        properties.setProperty("version", "1.3.0");
        StaticListableBeanFactory beanFactory = new StaticListableBeanFactory();
        beanFactory.addBean("buildProperties", new BuildProperties(properties));

        ObjectProvider<BuildProperties> provider = beanFactory.getBeanProvider(BuildProperties.class);

        assertThat(new SwaggerConfig().customOpenAPI(provider).getInfo().getVersion()).isEqualTo("1.3.0");
    }

    @Test
    void fallsBackToDevWithoutBuildInfo() {
        ObjectProvider<BuildProperties> provider = new StaticListableBeanFactory().getBeanProvider(BuildProperties.class);

        assertThat(new SwaggerConfig().customOpenAPI(provider).getInfo().getVersion()).isEqualTo("dev");
    }
}
