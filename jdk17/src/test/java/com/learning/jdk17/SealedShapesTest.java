package com.learning.jdk17;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SealedShapesTest {

    @Test
    void computesAreaForEachShape() {
        assertEquals(Math.PI * 4, Shape.area(new Shape.Circle(2)), 1e-9);
        assertEquals(6.0, Shape.area(new Shape.Rectangle(2, 3)), 1e-9);
        assertEquals(6.0, Shape.area(new Shape.Triangle(4, 3)), 1e-9);
    }
}
