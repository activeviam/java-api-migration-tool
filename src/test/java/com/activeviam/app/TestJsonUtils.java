/*
 * (C) ActiveViam 2024
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of ActiveViam. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */

package com.activeviam.app;

import static com.activeviam.util.JsonUtils.loadMappingFromJson;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class TestJsonUtils {

  @Test
  void testLoadingMapFromFile() {
    final String file = "hardcoded_mapping.json";

    final Map<String, String> mapping = loadMappingFromJson(file);

    assertThat(mapping)
        .containsExactlyInAnyOrderEntriesOf(
            Map.of(
                "test_key_1",
                "test_value_1",
                "test_key_2",
                "test_value_2",
                "test_key_3",
                "test_value_3",
                "test_key_4",
                "test_value_4",
                "test_key_5",
                "test_value_5"));
  }
}
