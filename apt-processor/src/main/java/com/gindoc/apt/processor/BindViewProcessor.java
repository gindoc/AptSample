package com.gindoc.apt.processor;

import com.gindoc.apt.annotation.BindView;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * @author :GIndoc
 * @date :Created in 2021/3/22
 */
@AutoService(Processor.class)
public class BindViewProcessor extends AbstractProcessor {

    private Messager mMessager;
    private Elements mElementUtils;

    private Map<String, ClassCreatorProxy> mProxyMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mMessager = processingEnv.getMessager();
        mElementUtils = processingEnv.getElementUtils();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> supportTypes = new LinkedHashSet<>();
        supportTypes.add(BindView.class.getCanonicalName());
        return supportTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, "processing...");
        mProxyMap.clear();
        // 得到所有注解elements
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(BindView.class);
        for (Element ele : elements) {
            //  这个元素是个field
            VariableElement variableElement = (VariableElement) ele;
            // 获取父元素
            TypeElement enclosingClass = (TypeElement) variableElement.getEnclosingElement();
            //获取元素所在类的名称
            String className = enclosingClass.getQualifiedName().toString();
            ClassCreatorProxy proxy = mProxyMap.get(className);
            if (proxy == null) {
                proxy = new ClassCreatorProxy(mElementUtils, enclosingClass);
                mProxyMap.put(className, proxy);
            }
            BindView annotation = variableElement.getAnnotation(BindView.class);
            int id = annotation.value();
            proxy.putElement(id, variableElement);
            mMessager.printMessage(Diagnostic.Kind.NOTE, "variableElement's name is " + variableElement.getSimpleName().toString() + "   ---- type is " + variableElement.asType().toString());
        }

        for (Map.Entry<String, ClassCreatorProxy> entry : mProxyMap.entrySet()) {
            ClassCreatorProxy proxy = entry.getValue();
            mMessager.printMessage(Diagnostic.Kind.NOTE, " --> create " + proxy.getProxyClassFullName());
            try {
                JavaFile file = JavaFile.builder(proxy.getPackageName(), proxy.generateJavaCodeByJavaPoet()).build();
                file.writeTo(processingEnv.getFiler());

//                JavaFileObject jfo = processingEnv.getFiler().createSourceFile(proxy.getProxyClassFullName(), proxy.getTypeElement());
//                Writer writer = jfo.openWriter();
//                writer.write(proxy.generateJavaCode());
//                writer.flush();
//                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
                mMessager.printMessage(Diagnostic.Kind.NOTE, " --> create " + proxy.getProxyClassFullName() + " error, " + e.getMessage());
            }
        }
        mMessager.printMessage(Diagnostic.Kind.NOTE, "process finish ...");
        return true;
    }

}
