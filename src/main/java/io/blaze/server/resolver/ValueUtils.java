package io.blaze.server.resolver;

import io.blaze.server.config.AppConfig;
import io.blaze.server.context.AppContext;
import org.yaml.snakeyaml.Yaml;

/**
 * @author Aravinth Sundaram on 21/12/23
 */
public class ValueUtils {

    private ValueUtils() {
        // utility class
    }

    public static Object extractValue(final String property,
                                      final Class<?> type) {
        final String[] propertyNameAndDefaultValue = property.split(":");
        final String propertyName = propertyNameAndDefaultValue[0];
        final String defaultValue = propertyNameAndDefaultValue.length > 1 ? propertyNameAndDefaultValue[1] : null;
        final String value = (String) AppContext.get(AppConfig.class).getProperties().get(propertyName);
        final String finalValue = ObjectUtils.firstNonNull(value, defaultValue);
        return finalValue == null ? null : new Yaml().loadAs(ObjectUtils.firstNonNull(value, defaultValue), type);
    }
}
