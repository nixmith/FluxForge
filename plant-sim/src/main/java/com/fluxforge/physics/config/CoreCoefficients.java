// CoreCoefficients.java
package com.fluxforge.physics.config;

public record CoreCoefficients(
        double beta,          // delayed neutron fraction (fractional, e.g. 0.0065)
        double lambda,        // 1/s
        double neutronGenTime // Λ, s
) {}
