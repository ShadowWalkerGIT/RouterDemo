package com.sw.fm.router.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.sw.router.Route;
import com.sw.router.RouteMeta;

import java.io.File;
import java.io.FileOutputStream;
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
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

public class RouteProcessor extends AbstractProcessor {
    private Filer mFiler;
    private Elements mElementUtils;
    private Map<String, Map<String, RouteMeta>> mGroupMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();
        mElementUtils = processingEnvironment.getElementUtils();
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
        generateXml();
    }

    private void generateXml() {
        FileOutputStream fos = null;
        XMLStreamWriter writer = null;
        try {
            File file = new File("router.xml");
            if (!file.exists()) {
                file.createNewFile();
            } else {
                file.delete();
                file.createNewFile();
            }
            fos = new FileOutputStream(file);
            writer = XMLOutputFactory.newInstance().createXMLStreamWriter(fos);
            writer.writeStartDocument("UTF-8", "1.0");
            writer.writeEndDocument();
            writer.writeCharacters("\n");
            writer.writeStartElement("routes");
            for (String group : mGroupMap.keySet()) {
                writer.writeCharacters("\n    ");
                Map<String, RouteMeta> dataGroup = mGroupMap.get(group);
                writer.writeStartElement("group");
                writer.writeAttribute("name", group);
                for (String path : dataGroup.keySet()) {
                    writer.writeCharacters("\n        ");
                    RouteMeta temp = dataGroup.get(path);
                    writer.writeStartElement("route");
                    writer.writeAttribute("group", group);
                    writer.writeAttribute("path", temp.getPath());
                    writer.writeAttribute("class", temp.getDestinationQualifiedName());
                    writer.writeEndElement();
                }
                writer.writeCharacters("\n    ");
                writer.writeEndElement();
            }
            writer.writeCharacters("\n");
            writer.writeEndElement();
            writer.flush();
            writer.close();
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void generateJavaFile() {
        // constructor
        MethodSpec.Builder cb = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("mGroupMap = new $T<>()", HashMap.class);
        int i = 0;
        cb.addComment("Key:path ; Value: RouteMeta");
        for (String group : mGroupMap.keySet()) {
            cb.addStatement("HashMap<String, RouteMeta> group$N = new HashMap<>()",String.valueOf(i));
            Map<String, RouteMeta> dataGroup = mGroupMap.get(group);
            for (String path : dataGroup.keySet()) {
                RouteMeta temp = dataGroup.get(path);
                cb.addStatement("group$N.put(\"$N\", new RouteMeta(\"$N\",\"$N\",\"$N\"))",String.valueOf(i), path, temp.getGroup(), temp.getPath(), temp.getDestinationQualifiedName());
            }
            cb.addStatement("mGroupMap.put(\"$N\", group$N)", group,String.valueOf(i));
            cb.addCode("\n");
            i++;
        }
        MethodSpec constructorMethodSpec = cb.build();

        // getClassByRoute(String path)
        MethodSpec getClassByPath = MethodSpec.methodBuilder("getClassByRoute")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "path")
                .returns(Class.class)
                .beginControlFlow("if (isStringEmpty(path) || !path.startsWith(\"/\"))")
                .addStatement("String msg = \"path cannot be null\"")
                .addStatement("print(msg)")
                .addStatement("throw new RuntimeException(msg)")
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
                .addStatement("String msg = \"group and path cannot be null\"")
                .addStatement("print(msg)")
                .addStatement("throw new RuntimeException(msg)")
                .endControlFlow()
                .beginControlFlow("if (mGroupMap.isEmpty())")
                .addStatement("String msg = \"route map is empty\"")
                .addStatement("print(msg)")
                .addStatement("throw new RuntimeException(msg)")
                .endControlFlow()
                .addStatement("HashMap<String , RouteMeta> routeMetas = mGroupMap.get(group)")
                .beginControlFlow("if (routeMetas == null || routeMetas.isEmpty())")
                .addStatement("String msg = \"cannot find group for key : \" + group")
                .addStatement("print(msg)")
                .addStatement("throw new RuntimeException(msg)")
                .endControlFlow()
                .addStatement("RouteMeta routeMeta = routeMetas.get(path)")
                .beginControlFlow("if (routeMeta == null)")
                .addStatement("String msg = \"cannot find path for key : \" + path")
                .addStatement("print(msg)")
                .addStatement("throw new RuntimeException(msg)")
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

        MethodSpec print = MethodSpec.methodBuilder("print")
                .addModifiers(Modifier.PRIVATE)
                .returns(void.class)
                .addParameter(String.class,"msg")
                .addStatement("System.out.println(\"*********error start*******\")")
                .addStatement("System.out.println(msg)")
                .addStatement("System.out.println(\"*********error end*******\")")
                .build();

        // getDefaultGroup
        MethodSpec getDefaultGroup = MethodSpec.methodBuilder("getDefaultGroup")
                .addModifiers(Modifier.PRIVATE)
                .addParameter(String.class, "path")
                .returns(String.class)
                .beginControlFlow("if (isStringEmpty(path) || !path.startsWith(\"/\"))")
                .addStatement("String msg = \"path cannot be null\"")
                .addStatement("print(msg)")
                .addStatement("throw new RuntimeException(msg)")
                .endControlFlow()
                .addStatement("return path.substring(1, path.indexOf(\"/\" , 1))")
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
                .addMethod(print)
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
            if (routeMetas.containsKey(routeMeta.getPath())) {
                String msg = new StringBuilder().append("\n")
                        .append("***************************************************************\n*\n")
                        .append("* path : " + (routeMeta.getGroup()+routeMeta.getPath()) + " has already been declared before. \n")
                        .append("* last declare : ").append(routeMetas.get(routeMeta.getPath()).getDestinationQualifiedName()).append("\n")
                        .append("* current declare : ").append(routeMeta.getDestinationQualifiedName()).append("\n*\n")
                        .append("***************************************************************\n")
                        .toString();
                throw new IllegalArgumentException(msg);
            }
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
