package ir.baho.framework.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;
import ir.baho.framework.converter.EnumConverter;
import ir.baho.framework.converter.PersianStringConverter;
import ir.baho.framework.enumeration.EnumType;
import ir.baho.framework.enumeration.EnumValue;
import ir.baho.framework.i18n.DateTimes;
import ir.baho.framework.i18n.FixedLocaleMessageResource;
import ir.baho.framework.i18n.MessageResource;
import ir.baho.framework.metadata.UserOptions;
import ir.baho.framework.service.CurrentUser;
import ir.baho.framework.service.impl.OptionsCurrentUser;
import ir.baho.framework.time.DateDeserializer;
import ir.baho.framework.time.DateSerializer;
import ir.baho.framework.time.DateTimeDeserializer;
import ir.baho.framework.time.DateTimeSerializer;
import ir.baho.framework.time.DurationDeserializer;
import ir.baho.framework.time.DurationSerializer;
import ir.baho.framework.time.TimeDeserializer;
import ir.baho.framework.time.TimeSerializer;
import ir.baho.framework.web.Headers;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@AutoConfiguration
@RequiredArgsConstructor
public class JacksonConfig {

    private final DateTimes dateTimes;
    private final CurrentUser currentUser;
    private final MessageResource messageResource;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static SimpleModule getUserOptionsModule(MessageResource messageResource, UserOptions options) {
        FixedLocaleMessageResource resource = new FixedLocaleMessageResource(messageResource, options.getLocale());
        CurrentUser user = new OptionsCurrentUser(options);
        DateTimes dateTimes = new DateTimes(user);
        return new SimpleModule("userOptionsModule").addDeserializer(String.class, new PersianStringConverter())
                .addSerializer(LocalDate.class, new DateSerializer(dateTimes, user))
                .addDeserializer(LocalDate.class, new DateDeserializer(dateTimes, user))
                .addSerializer(LocalDateTime.class, new DateTimeSerializer(dateTimes, user))
                .addDeserializer(LocalDateTime.class, new DateTimeDeserializer(dateTimes, user))
                .addSerializer(LocalTime.class, new TimeSerializer(dateTimes, user))
                .addDeserializer(LocalTime.class, new TimeDeserializer(dateTimes, user))
                .addSerializer(Duration.class, new DurationSerializer(dateTimes, user))
                .addDeserializer(Duration.class, new DurationDeserializer(dateTimes, user))
                .addSerializer(new JsonSerializer<Enum>() {
                    @Override
                    public void serialize(Enum e, JsonGenerator generator, SerializerProvider provider) throws IOException {
                        switch (options.getEnumType()) {
                            case NAME -> generator.writeString(e.name());
                            case TEXT ->
                                    generator.writeString(resource.getMessageOrDefault(EnumConverter.getPrefix(e) + "." + e.name(), e.name()));
                            default -> {
                                if (e instanceof EnumValue enumValue) {
                                    generator.writeObject(enumValue.getValue());
                                } else {
                                    generator.writeString(e.name());
                                }
                            }
                        }
                    }

                    @Override
                    public Class<Enum> handledType() {
                        return Enum.class;
                    }
                }).setDeserializerModifier(new BeanDeserializerModifier() {
                    @Override
                    public JsonDeserializer<?> modifyEnumDeserializer(DeserializationConfig config, JavaType type, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
                        if (beanDesc.getBeanClass().isEnum()) {
                            return new JsonDeserializer<>() {
                                @Override
                                public Object deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
                                    return EnumConverter.getEnum(resource, options.getEnumType(), (Class<Enum>) beanDesc.getBeanClass(), parser.getText());
                                }
                            };
                        }
                        return super.modifyEnumDeserializer(config, type, beanDesc, deserializer);
                    }
                });
    }

    @Bean
    public SimpleModule persianStringModule() {
        return new SimpleModule("persianStringModule").addDeserializer(String.class, new PersianStringConverter());
    }

    @Bean
    @SuppressWarnings({"rawtypes", "unchecked"})
    public SimpleModule enumModule() {
        return new SimpleModule("enumModule").addSerializer(new JsonSerializer<Enum>() {
            @Override
            public void serialize(Enum e, JsonGenerator generator, SerializerProvider provider) throws IOException {
                if (RequestContextHolder.getRequestAttributes() != null) {
                    try {
                        EnumType enumType = EnumType.valueOf(getEnumType().orElse(e instanceof EnumValue ? EnumType.VALUE.name() : EnumType.NAME.name()));
                        switch (enumType) {
                            case NAME -> generator.writeString(e.name());
                            case TEXT ->
                                    generator.writeString(messageResource.getMessageOrDefault(EnumConverter.getPrefix(e) + "." + e.name(), e.name()));
                            default -> {
                                if (e instanceof EnumValue enumValue) {
                                    generator.writeObject(enumValue.getValue());
                                } else {
                                    generator.writeString(e.name());
                                }
                            }
                        }
                    } catch (IllegalArgumentException ex) {
                        generator.writeString(e.name());
                    }
                } else {
                    generator.writeString(e.name());
                }
            }

            @Override
            public Class<Enum> handledType() {
                return Enum.class;
            }
        }).setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyEnumDeserializer(DeserializationConfig config, JavaType type, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
                if (beanDesc.getBeanClass().isEnum()) {
                    return new JsonDeserializer<>() {
                        @Override
                        public Object deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
                            return EnumConverter.getEnum(messageResource, currentUser.getEnumType(), (Class<Enum>) beanDesc.getBeanClass(), parser.getText());
                        }
                    };
                }
                return super.modifyEnumDeserializer(config, type, beanDesc, deserializer);
            }
        });
    }

    @Bean
    public SimpleModule dateModule() {
        return new SimpleModule("dateModule")
                .addSerializer(LocalDate.class, new DateSerializer(dateTimes, currentUser))
                .addDeserializer(LocalDate.class, new DateDeserializer(dateTimes, currentUser));
    }

    @Bean
    public SimpleModule datetimeModule() {
        return new SimpleModule("datetimeModule")
                .addSerializer(LocalDateTime.class, new DateTimeSerializer(dateTimes, currentUser))
                .addDeserializer(LocalDateTime.class, new DateTimeDeserializer(dateTimes, currentUser));
    }

    @Bean
    public SimpleModule timeModule() {
        return new SimpleModule("timeModule")
                .addSerializer(LocalTime.class, new TimeSerializer(dateTimes, currentUser))
                .addDeserializer(LocalTime.class, new TimeDeserializer(dateTimes, currentUser));
    }

    @Bean
    public SimpleModule durationModule() {
        return new SimpleModule("durationModule")
                .addSerializer(Duration.class, new DurationSerializer(dateTimes, currentUser))
                .addDeserializer(Duration.class, new DurationDeserializer(dateTimes, currentUser));
    }

    private Optional<String> getEnumType() {
        if (RequestContextHolder.getRequestAttributes() != null) {
            return Optional.ofNullable(((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getHeader(Headers.ENUM_TYPE));
        }
        return Optional.empty();
    }

}
