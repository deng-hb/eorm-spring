package com.denghb.eorm;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.*;

@SupportedAnnotationTypes({"*"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class EormProcessor extends AbstractProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
        if (SourceVersion.latest().compareTo(SourceVersion.RELEASE_6) > 0) {
            return SourceVersion.latest();
        } else {
            return SourceVersion.RELEASE_6;
        }
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        try {
            initOptions();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            for (Element element : roundEnv.getRootElements()) {
                doProcess(element);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private void doProcess(Element element) throws IOException {

        TypeElement typeElement = (TypeElement) element;
        PackageElement packageElement = processingEnv.getElementUtils().getPackageOf(element);
        String packageName = packageElement.getQualifiedName().toString();
        String className = typeElement.getSimpleName().toString();

        System.out.println("packageName > " + packageName);
        System.out.println("className > " + className);

        FileObject fileObject = processingEnv.getFiler().getResource(StandardLocation.SOURCE_PATH, packageName, className + JavaFileObject.Kind.SOURCE.extension);

        InputStream inputStream = fileObject.openInputStream();
        byte[] bytes = new byte[inputStream.available()];
        inputStream.read(bytes);
        inputStream.close();

        String str = new String(bytes);

        List<String> strs = new ArrayList<String>();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            // `""/**{` start
            if ('"' == c && '"' == str.charAt(i + 1) && '/' == str.charAt(i + 2)
                    && '*' == str.charAt(i + 3) && '*' == str.charAt(i + 4) && '{' == str.charAt(i + 5)) {
                int j = i + 6;
                StringBuffer sb = new StringBuffer();
                for (; j < str.length(); j++) {
                    // `}*/;` end
                    if ('}' == str.charAt(j) && '*' == str.charAt(j + 1)
                            && '/' == str.charAt(j + 2) && ';' == str.charAt(j + 3)) {
                        break;
                    }
                    sb.append(str.charAt(j));
                }
                strs.add(sb.toString());
                i = j;
            }
        }
        if (strs.isEmpty()) {
            return;
        }

        for (String s : strs) {
            s = s.replaceAll("\n", "");
            System.out.println(s);
            str = str.replaceFirst("\"\"/\\*\\*\\{", "\"" + s + "\"/**{");
        }

        String outputDir = System.getProperty("user.dir") + "/target/erom";
        File out = new File(outputDir);
        if (!out.exists()) {
            out.mkdirs();
        }

        compile(element.getSimpleName().toString(), outputDir, str);

    }

    private void compile(String name, String outputDir, String content) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

        StrSrcJavaObject srcObject = new StrSrcJavaObject(name, content);
        Iterable<? extends JavaFileObject> fileObjects = Arrays.asList(srcObject);

        optionMap.put("-d", outputDir);

        List<String> options = new ArrayList<String>();
        for (String key : optionMap.keySet()) {
            options.add(key);
            options.add(optionMap.get(key));
        }
        options.add("-proc:none");

        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, null, options, null, fileObjects);
        if (!task.call()) {
            System.err.println("Compile fail " + name);
        }

    }

    private Map<String, String> optionMap = new HashMap<String, String>();

    private void initOptions() throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        // TODO
        ClassLoader cl = getClass().getClassLoader();
        Class javacProcessingEnvironmentClass = Class.forName("com.sun.tools.javac.processing.JavacProcessingEnvironment", false, cl);
        if (javacProcessingEnvironmentClass.isInstance(processingEnv)) {
            Method getContextMethod = javacProcessingEnvironmentClass.getDeclaredMethod("getContext");
            Object tmpContext = getContextMethod.invoke(processingEnv);
            Class optionsClass = Class.forName("com.sun.tools.javac.util.Options", false, cl);
            Class contextClass = Class.forName("com.sun.tools.javac.util.Context", false, cl);
            Method instanceMethod = optionsClass.getDeclaredMethod("instance", new Class[]{contextClass});
            Object tmpOptions = instanceMethod.invoke(null, tmpContext);
            if (tmpOptions != null) {
                Method getMethod = optionsClass.getDeclaredMethod("get", new Class[]{String.class});
                // "-Xlint:none", "-target", "-g", "-source", "save-parameter-names",  "-Xlint:-options",
                String[] args = {"-classpath", "-sourcepath", "-encoding"};
                for (String s : args) {
                    Object result = getMethod.invoke(tmpOptions, s);
                    if (null != result) {
                        optionMap.put(s, String.valueOf(result));
                    }
                }

            }
        }

    }

    private static class StrSrcJavaObject extends SimpleJavaFileObject {
        private String content;

        public StrSrcJavaObject(String name, String content) {
            super(URI.create("string:///" + name + Kind.SOURCE.extension), Kind.SOURCE);
            this.content = content;
        }

        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return content;
        }
    }
}
