package ir.baho.framework.i18n;

import java.util.Locale;

public class FixedLocaleMessageResource extends MessageResource {

    private final MessageResource messageResource;
    private final Locale locale;

    public FixedLocaleMessageResource(MessageResource messageResource, Locale locale) {
        super(messageResource);
        this.messageResource = messageResource;
        this.locale = locale;
    }

    @Override
    public String getMessage(String key, Object... params) {
        return messageResource.getMessage(key, params, locale);
    }

    @Override
    public String getMessageOrDefault(String key, String defaultMessage, Object... params) {
        return messageResource.getMessage(key, params, defaultMessage, locale);
    }

}
