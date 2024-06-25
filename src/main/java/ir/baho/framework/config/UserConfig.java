package ir.baho.framework.config;

import ir.baho.framework.service.CurrentUser;
import ir.baho.framework.service.impl.DefaultCurrentUser;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class UserConfig {

    @Bean
    @ConditionalOnMissingBean
    CurrentUser currentUser() {
        return new DefaultCurrentUser();
    }

}
