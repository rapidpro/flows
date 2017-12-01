package io.rapidpro.flows.utils;

import com.google.gson.JsonElement;

/**
 * Interface for anything that can be converted to JSON
 */
public interface Jsonizable {

    JsonElement toJson();
}
