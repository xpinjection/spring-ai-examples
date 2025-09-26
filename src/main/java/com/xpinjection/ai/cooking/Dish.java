package com.xpinjection.ai.cooking;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Dish(@JsonProperty(required = true) String name,
                   @JsonProperty(required = true) List<Ingredient> ingredients,
                   @JsonProperty(required = true) String recipe) {}
