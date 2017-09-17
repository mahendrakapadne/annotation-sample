package org.home.sample.annotation.processor;

import com.google.testing.compile.JavaFileObjects;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

@RunWith(JUnit4.class)
public class ProcessorTest {
    private StubProcessor mProcessor;

    @Before
    public void init() {
        mProcessor = new StubProcessor();
    }

    @Test
    public void generateEmptyStubbedClass() {
        assert_().about(javaSource())
                .that(JavaFileObjects.forSourceLines("demo.Teapot",
                        "package demo;",
                        "@demo.Stub public interface Teapot {}"))
                .processedWith(mProcessor)
                .compilesWithoutError()
                .and()
                .generatesSources(JavaFileObjects
                        .forSourceLines("demo",
                                "package demo;",
                                "public class StubTeapot implements Teapot {",
                                "}"
                        ));
    }

    @Test
    public void errorForAnnotatedClass() {
        assert_().about(javaSource())
                .that(JavaFileObjects.forSourceLines("demo.Teapot",
                        "package demo;",
                        "@demo.Stub public class Teapot {}"))
                .processedWith(mProcessor)
                .failsToCompile()
                .withErrorContaining("Please just annotate interfaces.");
    }

    @Test
    public void generateStubbedClass() {
        assert_().about(javaSource())
                .that(JavaFileObjects.forSourceLines("demo.Teapot",
                        "package demo;",
                        "@demo.Stub public interface Teapot {",
                        "void boilWater();",
                        "boolean isBoiling();",
                        "}"))
                .processedWith(mProcessor)
                .compilesWithoutError()
                .and()
                .generatesSources(JavaFileObjects
                        .forSourceLines("demo",
                                "package demo;",
                                "public class StubTeapot implements Teapot {",
                                "@Override public void boilWater() {}",
                                "@Override public boolean isBoiling() { return false; }",
                                "}"
                        ));
    }
}