package com.nathanmcunha.minispring.common;

import com.nathanmcunha.minispring.error.FrameworkError;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JsonSerializer {

  private static final Map<Class<?>, Function<String, Object>> CONVERTERS = new HashMap<>();

  static {
    CONVERTERS.put(String.class, s -> s);

    CONVERTERS.put(int.class, Integer::parseInt);
    CONVERTERS.put(Integer.class, Integer::parseInt);

    CONVERTERS.put(long.class, Long::parseLong);
    CONVERTERS.put(Long.class, Long::parseLong);

    CONVERTERS.put(boolean.class, Boolean::parseBoolean);
    CONVERTERS.put(Boolean.class, Boolean::parseBoolean);

    // CONVERTERS.put(LocalDate.class, LocalDate::parse);
  }

  public String serialize(Object obj) {
    if (!(obj instanceof Record r)) return "";

    var components = r.getClass().getRecordComponents();

    String fields =
        Arrays.stream(components)
            .map(
                c -> {
                  try {
                    String name = c.getName();
                    Object value = c.getAccessor().invoke(r);
                    return "\"" + name + "\": \"" + value + "\"";
                  } catch (Exception e) {
                    return "";
                  }
                })
            .filter(s -> !s.isEmpty())
            .collect(Collectors.joining(", "));

    return "{ " + fields + " }";
  }

  public <T> Result<T, FrameworkError> deserialize(String json, Class<T> targetClass) {
    if (!targetClass.isRecord()) {
      return Result.failure(
          new FrameworkError.JsonConversionFailed("Only record type classes are permited", null));
    }

    var components = targetClass.getRecordComponents();
    Object[] args = new Object[components.length];

    for (int i = 0; i < components.length; i++) {
      var comp = components[i];
      String fieldName = comp.getName();
      Class<?> type = comp.getType();

      String rawValue = extractValueFromJson(json, fieldName);
      if (rawValue == null) {
        return Result.failure(
            new FrameworkError.JsonConversionFailed("Mandatory field missing: " + fieldName, null));
      }
      var converter = CONVERTERS.get(type);
      if (converter == null)
        return Result.failure(
            new FrameworkError.JsonConversionFailed("Type not supported " + type.getName(), null));

      args[i] = converter.apply(rawValue);
    }
    var constructor = targetClass.getDeclaredConstructors()[0];
    T instance;
    try {
      instance = targetClass.cast(constructor.newInstance(args));
    } catch (Exception e) {
      return Result.failure(new FrameworkError.JsonConversionFailed("Error in deserialize ", e));
    }

    return Result.success(instance);
  }

  private String extractValueFromJson(String json, String fieldName) {
    String regex = "\"" + fieldName + "\"\\s*:\\s*\"([^\"]*)\"";
    var matcher = Pattern.compile(regex).matcher(json);
    return matcher.find() ? matcher.group(1) : null;
  }
}
