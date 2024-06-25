package ir.baho.framework.config;

import ir.baho.framework.time.DateReadingConverter;
import ir.baho.framework.time.DateWritingConverter;
import ir.baho.framework.time.DurationReadingConverter;
import ir.baho.framework.time.DurationWritingConverter;
import ir.baho.framework.time.TimeReadingConverter;
import ir.baho.framework.time.TimeWritingConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.Arrays;

@ConditionalOnClass(name = "org.springframework.data.mongodb.core.convert.MongoCustomConversions")
@AutoConfiguration
public class MongoConfig {

    @Bean
    MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(Arrays.asList(
                new DateWritingConverter(), new DateReadingConverter(),
                new TimeWritingConverter(), new TimeReadingConverter(),
                new DurationWritingConverter(), new DurationReadingConverter()
        ));
    }

}
