package com.xpinjection.ai.cooking;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Ingredient(@JsonProperty(required = true) String name,
                         @JsonProperty(required = true) String amount,
                         @JsonProperty(required = true) int calories) {}
