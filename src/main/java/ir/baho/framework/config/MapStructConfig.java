package ir.baho.framework.config;

import org.mapstruct.Builder;
import org.mapstruct.MapperConfig;

@MapperConfig(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface MapStructConfig {

}
