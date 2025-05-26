// PhysicsConfigLoader.java
package com.fluxforge.physics.config;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.IOException;
import java.nio.file.Path;

public final class PhysicsConfigLoader {
    private static final YAMLMapper MAPPER = new YAMLMapper();

    public static CoreCoefficients loadCoeffs(Path yaml) throws IOException {
        return MAPPER.readValue(yaml.toFile(), CoreCoefficients.class);
    }
}
