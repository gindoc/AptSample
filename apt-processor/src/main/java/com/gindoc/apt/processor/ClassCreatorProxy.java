package com.gindoc.apt.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

/**
 * @author :GIndoc
 * @date :Created in 2021/3/22
 */
class ClassCreatorProxy {

    private final String mPackageName;
    private final String mBindingClassName;
    private TypeElement mTypeElement;
    private Map<Integer, VariableElement> mVariableElementMap = new HashMap<>();

    public ClassCreatorProxy(Elements elementsUtil, TypeElement classElement) {
        this.mTypeElement = classElement;
        PackageElement packageElement = elementsUtil.getPackageOf(classElement);
        mPackageName = packageElement.getQualifiedName().toString();
        mBindingClassName = classElement.getSimpleName().toString() + "_ViewBinding";
    }

    public void putElement(int id, VariableElement element) {
        mVariableElementMap.put(id, element);
    }

    public String getProxyClassFullName() {
        return mPackageName + "." + mBindingClassName;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public TypeElement getTypeElement() {
        return mTypeElement;
    }

    public String generateJavaCode() {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(mPackageName).append(";\n\n")
                .append("public class ").append(mBindingClassName).append(" {\n\n");

        generateMethods(sb);

        sb.append("\n").append("}\n");

        return sb.toString();
    }

    private void generateMethods(StringBuilder sb) {
        sb.append("\rpublic void bind(").append(mTypeElement.getQualifiedName().toString()).append(" host) {\n");
        for (Map.Entry<Integer, VariableElement> entry : mVariableElementMap.entrySet()) {
            VariableElement variableElement = entry.getValue();
            String name = variableElement.getSimpleName().toString();
            String type = variableElement.asType().toString();
            sb.append("\r\rhost.").append(name).append(" = ");
            sb.append("(").append(type).append(")(((android.app.Activity)host).findViewById(").append(entry.getKey()).append("));\n");
        }
        sb.append("\r}\n");
    }


    public TypeSpec generateJavaCodeByJavaPoet() {
        return TypeSpec.classBuilder(mBindingClassName)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(generateMethodCodeByJavaPoet())
                .build();
    }

    public MethodSpec generateMethodCodeByJavaPoet() {
        ClassName host = ClassName.bestGuess(mTypeElement.getQualifiedName().toString());
        MethodSpec.Builder builder = MethodSpec.methodBuilder("bind")
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(host, "host");
        for (Map.Entry<Integer, VariableElement> entry : mVariableElementMap.entrySet()) {
            VariableElement element = entry.getValue();
            String name = element.getSimpleName().toString();
            String type = element.asType().toString();
            builder.addCode("host." + name + " = (" + type + ")(host.findViewById(" + entry.getKey() + "));\n");
        }
        return builder.build();
    }

}
