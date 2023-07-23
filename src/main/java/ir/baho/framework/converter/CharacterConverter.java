package ir.baho.framework.converter;

import java.util.List;

public class CharacterConverter extends StringConverter<Character> {

    public CharacterConverter() {
        super(CharacterConverter.class.getSimpleName());
    }

    @Override
    public Character convert(String source) {
        return source.charAt(0);
    }

    @Override
    protected List<Class<?>> supportedTypes() {
        return List.of(char.class, Character.class);
    }

}
