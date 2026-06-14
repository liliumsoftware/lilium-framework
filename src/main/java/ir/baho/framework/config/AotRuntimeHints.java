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

        reflection.registerType(BaseEntitySimple.class);
        reflection.registerType(BaseDocumentSimple.class);
        reflection.registerType(BaseSubDocument.class);
        reflection.registerType(BaseRevisionEntity.class);
        reflection.registerType(BaseView.class);
        reflection.registerType(BaseIdDto.class);
        reflection.registerType(RevisionMetadataDto.class);
        reflection.registerType(boolean.class);
        reflection.registerType(Boolean.class);
        reflection.registerType(byte.class);
        reflection.registerType(Byte.class);
        reflection.registerType(short.class);
        reflection.registerType(Short.class);
        reflection.registerType(int.class);
        reflection.registerType(Integer.class);
        reflection.registerType(long.class);
        reflection.registerType(Long.class);
        reflection.registerType(float.class);
        reflection.registerType(Float.class);
        reflection.registerType(double.class);
        reflection.registerType(Double.class);
        reflection.registerType(char.class);
        reflection.registerType(Character.class);
        reflection.registerType(String.class);
        reflection.registerType(LocalDate.class);
        reflection.registerType(LocalDateTime.class);
        reflection.registerType(LocalTime.class);
        reflection.registerType(Duration.class);

        ResourceHints resources = hints.resources();
        resources.registerPattern("common-messages.properties");
        resources.registerPattern("common-messages_fa.properties");
        resources.registerPattern("**/pom.properties");
        resources.registerPattern("com/ibm/icu/impl/data/icudata/*");
    }

}
