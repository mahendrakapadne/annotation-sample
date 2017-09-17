package org.home.sample.annotation.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import demo.Stub;


import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@AutoService(Processor.class)
public class StubProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(Stub.class)) {
            if (element.getKind() == ElementKind.INTERFACE) {
                stubOutInterface((TypeElement) element);
            } else {
                processingEnv.getMessager()
                        .printMessage(Diagnostic.Kind.ERROR,
                                "Please just annotate interfaces.", element);
            }
        }
        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(Stub.class.getCanonicalName());
    }

    private void stubOutInterface(TypeElement superInterface) {
        String stubName = "Stub" + superInterface.getSimpleName();
        TypeSpec stubClass = TypeSpec.classBuilder(stubName)
                .addSuperinterface(ClassName.get(superInterface))
                .addModifiers(Modifier.PUBLIC)
                .addMethods(createStubbedMethods(superInterface))
                .build();

        String packageName = superInterface.getEnclosingElement()
                .getSimpleName().toString();
        JavaFile javaFile = JavaFile.builder(packageName, stubClass)
                .skipJavaLangImports(true)
                .build();

        try {
            // write our type to disk
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Iterable<MethodSpec> createStubbedMethods(TypeElement superInterface) {
        List<MethodSpec> methods = new ArrayList<>();
        for (Element element : superInterface.getEnclosedElements()) {
            if (element.getKind() == ElementKind.METHOD) {
                methods.add(createStubbedMethod((ExecutableElement) element));
            }
        }
        return methods;
    }

    private MethodSpec createStubbedMethod(ExecutableElement method) {
        return MethodSpec.methodBuilder(method.getSimpleName().toString())
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get(method.getReturnType()))
                .addCode(getDefaultReturnValue(method.getReturnType()))
                .build();
    }

    private CodeBlock getDefaultReturnValue(TypeMirror type) {
        if (type.getKind() == TypeKind.VOID) {
            return CodeBlock.builder().addStatement("// do nothing").build();
        } else if (type.getKind() == TypeKind.BOOLEAN) {
            return CodeBlock.builder().addStatement("return $L", false).build();
        } /* TODO other types  */
        return CodeBlock.builder().addStatement("return null").build();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
