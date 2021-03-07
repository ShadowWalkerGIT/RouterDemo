package com.sw.router;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class RouteProcessor extends AbstractProcessor {
    private Filer mFiler;
    private Elements mElementUtils;
    private Types mTypeUtils;
    private Map<String, Map<String, RouteMeta>> mGroupMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();
        mElementUtils = processingEnvironment.getElementUtils();
        mTypeUtils = processingEnvironment.getTypeUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (!set.isEmpty()) {
            Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Route.class);
            if (!elements.isEmpty()) {
                processRoute(elements);
            }
            return true;
        }
        return false;
    }

    private void processRoute(Set<? extends Element> elements) {
        for (Element element : elements) {
            Route annotation = element.getAnnotation(Route.class);
            String path = annotation.path();
            String group = annotation.group();
            RouteMeta routeMeta = new RouteMeta(group, path);
            if (verifyRoute(routeMeta)) {
                String className = element.getSimpleName().toString();
                PackageElement packageOfElement = mElementUtils.getPackageOf(element);
                String classPackage = packageOfElement.getQualifiedName().toString();
                routeMeta.setDestinationQualifiedName(new StringBuilder().append(classPackage).append(".").append(className).toString());
                saveRoute(routeMeta);
            }
        }
        generateJavaFile();
    }

    private void generateJavaFile() {
        // constructor
        MethodSpec.Builder cb = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("mGroupMap = new $T<>()", HashMap.class);
        for (String group : mGroupMap.keySet()) {
            cb.addStatement("HashMap<String , RouteMeta> data = new HashMap<>()");
            Map<String, RouteMeta> dataGroup = mGroupMap.get(group);
            for (String path : dataGroup.keySet()) {
                RouteMeta temp = dataGroup.get(path);
                cb.addStatement("data.put(\"$N\",new RouteMeta(\"$N\",\"$N\",\"$N\"))", path, temp.getGroup(), temp.getPath(), temp.getDestinationQualifiedName());
            }
            cb.addStatement("mGroupMap.put(\"$N\" , data)", group);
        }
        MethodSpec constructorMethodSpec = cb.build();

        // getClassByRoute(String path)
        MethodSpec getClassByPath = MethodSpec.methodBuilder("getClassByRoute")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "path")
                .returns(Class.class)
                .beginControlFlow("if (isStringEmpty(path) || !path.startsWith(\"/\"))")
                .addStatement("throw new RuntimeException(\"path cannot be null\")")
                .endControlFlow()
                .addStatement("String group = getDefaultGroup(path)")
                .addStatement("return getClassByRoute(group , path.substring(1 + group.length()))")
                .build();

        // getClassByRoute(String group , String path)
        MethodSpec getClassByGroupAndPath = MethodSpec.methodBuilder("getClassByRoute")
                .addModifiers(Modifier.PUBLIC)
                .returns(Class.class)
                .addParameter(String.class, "group")
                .addParameter(String.class, "path")
                .beginControlFlow("if (isStringEmpty(group) || isStringEmpty(path))")
                .addStatement("throw new RuntimeException(\"group and path cannot be null\")")
                .endControlFlow()
                .beginControlFlow("if (mGroupMap.isEmpty())")
                .addStatement("throw new RuntimeException(\"route map is empty\")")
                .endControlFlow()
                .addStatement("HashMap<String , RouteMeta> routeMetas = mGroupMap.get(group)")
                .beginControlFlow("if (routeMetas == null || routeMetas.isEmpty())")
                .addStatement("throw new RuntimeException(\"cannot find group for key : \" + group)")
                .endControlFlow()
                .addStatement("RouteMeta routeMeta = routeMetas.get(path)")
                .beginControlFlow("if (routeMeta == null)")
                .addStatement("throw new RuntimeException( \"cannot find path for group : \" + path)")
                .endControlFlow()
                .addStatement("Class destinationClass = routeMeta.getDestinationClass()")
                .beginControlFlow("if (destinationClass != null)")
                .addStatement("return destinationClass")
                .endControlFlow()
                .beginControlFlow("try")
                .addStatement("destinationClass = Class.forName(routeMeta.getDestinationQualifiedName())")
                .addStatement("routeMeta.setDestinationClass(destinationClass)")
                .addStatement("return destinationClass")
                .endControlFlow()
                .beginControlFlow("catch (Exception e)")
                .addStatement("e.printStackTrace()")
                .addStatement("throw new RuntimeException( e )")
                .endControlFlow()
                .build();

        // isStringEmpty
        MethodSpec isStringEmpty = MethodSpec.methodBuilder("isStringEmpty")
                .addModifiers(Modifier.PRIVATE)
                .returns(Boolean.class)
                .addParameter(String.class, "text")
                .addStatement("return text == null || text.length() == 0")
                .build();

        // getDefaultGroup
        MethodSpec getDefaultGroup = MethodSpec.methodBuilder("getDefaultGroup")
                .addModifiers(Modifier.PRIVATE)
                .addParameter(String.class, "path")
                .returns(String.class)
                .beginControlFlow("if (isStringEmpty(path) || !path.startsWith(\"/\"))")
                .addStatement("throw new RuntimeException(\"path cannot be null\")")
                .endControlFlow()
                .addStatement("return path.substring( 1, path.indexOf(\"/\" , 1))")
                .build();

        // mGroupMap HashMap<String,HashMap<String,RouteMeta> mGroupMap

        ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(ClassName.get(HashMap.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(ClassName.get(HashMap.class),
                        ClassName.get(String.class),
                        ClassName.get(RouteMeta.class)));


        // class
        TypeSpec classTypeSpec = TypeSpec.classBuilder("Router")
                .addModifiers(Modifier.PUBLIC)
                .addMethod(constructorMethodSpec)
                .addMethod(getClassByGroupAndPath)
                .addMethod(getClassByPath)
                .addMethod(getDefaultGroup)
                .addMethod(isStringEmpty)
                .addField(FieldSpec.builder(parameterizedTypeName, "mGroupMap").addModifiers(Modifier.PRIVATE).build())
                .build();
        JavaFile javaFile = JavaFile.builder("com.sw.router", classTypeSpec).build();
        try {
            javaFile.writeTo(mFiler);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void saveRoute(RouteMeta routeMeta) {
        Map<String, RouteMeta> routeMetas = mGroupMap.get(routeMeta.getGroup());
        if (routeMetas == null || routeMetas.isEmpty()) {
            routeMetas = new HashMap<>();
            routeMetas.put(routeMeta.getPath(), routeMeta);
            mGroupMap.put(routeMeta.getGroup(), routeMetas);
        } else {
            routeMetas.put(routeMeta.getPath(), routeMeta);
        }
    }

    private boolean verifyRoute(RouteMeta routeMeta) {
        String path = routeMeta.getPath();
        if ((isStringEmpty(path) || !path.startsWith("/"))) {
            return false;
        }
        if (isStringEmpty(routeMeta.getGroup())) {
            try {
                String defaultGroup = path.substring(1, path.indexOf("/", 1));
                if (isStringEmpty(defaultGroup)) {
                    return false;
                }
                routeMeta.setGroup(defaultGroup);
                routeMeta.setPath(path.substring(1 + defaultGroup.length()));
                return true;
            } catch (Exception e) {
                System.out.println("auto extract group failed : " + e.getMessage());
            }
        }
        return true;
    }

    private boolean isStringEmpty(String text) {
        return text == null || text.length() == 0;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        LinkedHashSet<String> types = new LinkedHashSet<>();
        types.add(Route.class.getCanonicalName());
        return types;
    }
}
