/*
 * (C) ActiveViam 2024
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of ActiveViam. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */

package com.activeviam.util.private_;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class JsonUtils {

  private JsonUtils() {}

  public static Map<String, String> loadMappingFromJson(final String hardcodedMapping) {
    final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    try (final InputStream input = classLoader.getResourceAsStream(hardcodedMapping)) {
      return jsonStreamToMap(input);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  private static Map<String, String> jsonStreamToMap(final InputStream stream) throws IOException {
    final ObjectMapper mapper = new ObjectMapper();
    return mapper.readValue(stream, HashMap.class);
  }
}
