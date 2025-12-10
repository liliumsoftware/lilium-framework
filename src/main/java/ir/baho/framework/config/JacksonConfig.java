package ir.baho.framework.config;

import com.fasterxml.jackson.annotation.JsonProperty;
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
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.ValueDeserializerModifier;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.StdSerializer;

import java.lang.reflect.Field;
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
                .addSerializer(new UserEnumSerializer(options, resource)).setDeserializerModifier(new ValueDeserializerModifier() {
                    @Override
                    public ValueDeserializer<?> modifyEnumDeserializer(DeserializationConfig config, JavaType type, BeanDescription.Supplier beanDesc, ValueDeserializer<?> deserializer) {
                        if (beanDesc.getBeanClass().isEnum()) {
                            return new UserEnumDeserializer(beanDesc, resource, options);
                        }
                        return super.modifyEnumDeserializer(config, type, beanDesc, deserializer);
                    }
                });
    }

    @Bean
    SimpleModule persianStringModule() {
        return new SimpleModule("persianStringModule").addDeserializer(String.class, new PersianStringConverter());
    }

    @Bean
    SimpleModule enumModule() {
        return new SimpleModule("enumModule").addSerializer(new EnumSerializer()).setDeserializerModifier(new ValueDeserializerModifier() {
            @Override
            public ValueDeserializer<?> modifyEnumDeserializer(DeserializationConfig config, JavaType type, BeanDescription.Supplier beanDesc, ValueDeserializer<?> deserializer) {
                if (beanDesc.getBeanClass().isEnum()) {
                    return new EnumDeserializer(beanDesc);
                }
                return super.modifyEnumDeserializer(config, type, beanDesc, deserializer);
            }
        });
    }

    @Bean
    SimpleModule dateModule() {
        return new SimpleModule("dateModule")
                .addSerializer(LocalDate.class, new DateSerializer(dateTimes, currentUser))
                .addDeserializer(LocalDate.class, new DateDeserializer(dateTimes, currentUser));
    }

    @Bean
    SimpleModule datetimeModule() {
        return new SimpleModule("datetimeModule")
                .addSerializer(LocalDateTime.class, new DateTimeSerializer(dateTimes, currentUser))
                .addDeserializer(LocalDateTime.class, new DateTimeDeserializer(dateTimes, currentUser));
    }

    @Bean
    SimpleModule timeModule() {
        return new SimpleModule("timeModule")
                .addSerializer(LocalTime.class, new TimeSerializer(dateTimes, currentUser))
                .addDeserializer(LocalTime.class, new TimeDeserializer(dateTimes, currentUser));
    }

    @Bean
    SimpleModule durationModule() {
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

    @SuppressWarnings("rawtypes")
    private static class UserEnumSerializer extends StdSerializer<Enum> {

        private final UserOptions options;
        private final FixedLocaleMessageResource resource;

        public UserEnumSerializer(UserOptions options, FixedLocaleMessageResource resource) {
            super(Enum.class);
            this.options = options;
            this.resource = resource;
        }

        @Override
        public void serialize(Enum e, JsonGenerator generator, SerializationContext provider) {
            try {
                JsonProperty jsonProperty = e.getClass().getField(e.name()).getAnnotation(JsonProperty.class);
                if (jsonProperty != null) {
                    generator.writeString(jsonProperty.value());
                } else {
                    switch (options.getEnumType()) {
                        case NAME -> generator.writeString(e.name());
                        case TEXT ->
                                generator.writeString(resource.getMessageOrDefault(EnumConverter.getPrefix(e) + "." + e.name(), e.name()));
                        default -> {
                            if (e instanceof EnumValue enumValue) {
                                generator.writePOJO(enumValue.getValue());
                            } else {
                                generator.writeString(e.name());
                            }
                        }
                    }
                }
            } catch (NoSuchFieldException ex) {
                generator.writeString(e.name());
            }
        }

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static class UserEnumDeserializer extends StdDeserializer<Enum> {
        private final BeanDescription.Supplier beanDesc;
        private final FixedLocaleMessageResource resource;
        private final UserOptions options;

        public UserEnumDeserializer(BeanDescription.Supplier beanDesc, FixedLocaleMessageResource resource, UserOptions options) {
            super(Enum.class);
            this.beanDesc = beanDesc;
            this.resource = resource;
            this.options = options;
        }

        @Override
        public Enum deserialize(JsonParser parser, DeserializationContext deserializationContext) {
            String jsonValue = parser.getString();
            Class<?> enumClass = beanDesc.getBeanClass();
            for (Field field : enumClass.getDeclaredFields()) {
                JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
                if (jsonProperty != null && jsonProperty.value().equals(jsonValue)) {
                    try {
                        return Enum.valueOf((Class<Enum>) enumClass, field.getName());
                    } catch (IllegalArgumentException _) {
                    }
                }
            }
            return EnumConverter.getEnum(resource, options.getEnumType(), (Class<Enum>) beanDesc.getBeanClass(), jsonValue);
        }

    }

    @SuppressWarnings("rawtypes")
    private class EnumSerializer extends StdSerializer<Enum> {

        public EnumSerializer() {
            super(Enum.class);
        }

        @Override
        public void serialize(Enum e, JsonGenerator generator, SerializationContext provider) {
            try {
                JsonProperty jsonProperty = e.getClass().getField(e.name()).getAnnotation(JsonProperty.class);
                if (jsonProperty != null) {
                    generator.writeString(jsonProperty.value());
                } else {
                    if (RequestContextHolder.getRequestAttributes() != null) {
                        try {
                            EnumType enumType = EnumType.valueOf(getEnumType().orElse(e instanceof EnumValue ? EnumType.VALUE.name() : EnumType.NAME.name()));
                            switch (enumType) {
                                case NAME -> generator.writeString(e.name());
                                case TEXT ->
                                        generator.writeString(messageResource.getMessageOrDefault(EnumConverter.getPrefix(e) + "." + e.name(), e.name()));
                                default -> {
                                    if (e instanceof EnumValue enumValue) {
                                        generator.writePOJO(enumValue.getValue());
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
            } catch (NoSuchFieldException ex) {
                generator.writeString(e.name());
            }
        }

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private class EnumDeserializer extends StdDeserializer<Enum> {

        private final BeanDescription.Supplier beanDesc;

        public EnumDeserializer(BeanDescription.Supplier beanDesc) {
            super(Object.class);
            this.beanDesc = beanDesc;
        }

        @Override
        public Enum deserialize(JsonParser p, DeserializationContext ctxt) {
            String jsonValue = p.getString();
            Class<?> enumClass = beanDesc.getBeanClass();
            for (Field field : enumClass.getDeclaredFields()) {
                JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
                if (jsonProperty != null && jsonProperty.value().equals(jsonValue)) {
                    try {
                        return Enum.valueOf((Class<Enum>) enumClass, field.getName());
                    } catch (IllegalArgumentException _) {
                    }
                }
            }
            return EnumConverter.getEnum(messageResource, currentUser.enumType(), (Class<Enum>) beanDesc.getBeanClass(), p.getString());
        }

    }

}
