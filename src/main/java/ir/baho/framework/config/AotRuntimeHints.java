package ir.baho.framework.config;

import ir.baho.framework.domain.BaseDocumentSimple;
import ir.baho.framework.domain.BaseEntitySimple;
import ir.baho.framework.domain.BaseEntitySimple_;
import ir.baho.framework.domain.BaseEntity_;
import ir.baho.framework.domain.BaseFile_;
import ir.baho.framework.domain.BaseRevisionEntity;
import ir.baho.framework.domain.BaseRevisionEntity_;
import ir.baho.framework.domain.BaseSubDocument;
import ir.baho.framework.domain.BaseView;
import ir.baho.framework.domain.BaseView_;
import ir.baho.framework.dto.BaseIdDto;
import ir.baho.framework.dto.RevisionMetadataDto;
import ir.baho.framework.i18n.DateTimes;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.ReflectionHints;
import org.springframework.aot.hint.ResourceHints;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.SerializationHints;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

class AotRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
        ReflectionHints reflection = hints.reflection();
        reflection.registerType(
                DateTimes.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS,
                MemberCategory.INVOKE_PUBLIC_METHODS,
                MemberCategory.ACCESS_DECLARED_FIELDS
        );
        reflection.registerType(BaseEntitySimple_.class, MemberCategory.ACCESS_DECLARED_FIELDS);
        reflection.registerType(BaseEntity_.class, MemberCategory.ACCESS_DECLARED_FIELDS);
        reflection.registerType(BaseFile_.class, MemberCategory.ACCESS_DECLARED_FIELDS);
        reflection.registerType(BaseRevisionEntity_.class, MemberCategory.ACCESS_DECLARED_FIELDS);
        reflection.registerType(BaseView_.class, MemberCategory.ACCESS_DECLARED_FIELDS);

        SerializationHints serialization = hints.serialization();
        serialization.registerType(BaseEntitySimple.class);
        serialization.registerType(BaseDocumentSimple.class);
        serialization.registerType(BaseSubDocument.class);
        serialization.registerType(BaseRevisionEntity.class);
        serialization.registerType(BaseView.class);
        serialization.registerType(BaseIdDto.class);
        serialization.registerType(RevisionMetadataDto.class);
        serialization.registerType(boolean.class);
        serialization.registerType(Boolean.class);
        serialization.registerType(byte.class);
        serialization.registerType(Byte.class);
        serialization.registerType(short.class);
        serialization.registerType(Short.class);
        serialization.registerType(int.class);
        serialization.registerType(Integer.class);
        serialization.registerType(long.class);
        serialization.registerType(Long.class);
        serialization.registerType(float.class);
        serialization.registerType(Float.class);
        serialization.registerType(double.class);
        serialization.registerType(Double.class);
        serialization.registerType(char.class);
        serialization.registerType(Character.class);
        serialization.registerType(String.class);
        serialization.registerType(LocalDate.class);
        serialization.registerType(LocalDateTime.class);
        serialization.registerType(LocalTime.class);
        serialization.registerType(Duration.class);

        ResourceHints resources = hints.resources();
        resources.registerPattern("common-messages.properties");
        resources.registerPattern("common-messages_fa.properties");
        resources.registerPattern("**/pom.properties");
        resources.registerPattern("com/ibm/icu/impl/data/icudata/*");
    }

}
