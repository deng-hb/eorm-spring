package com.denghb.eorm;

import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


@SupportedAnnotationTypes("*")
public class EormProcessor extends AbstractProcessor {

    private JavacProcessingEnvironment javacProcessingEnv;
    private Elements elements;
    private Filer filer;
    private Trees trees;
    private TreeMaker maker;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        javacProcessingEnv = (JavacProcessingEnvironment) processingEnv;

        elements = javacProcessingEnv.getElementUtils();
        filer = javacProcessingEnv.getFiler();

        trees = Trees.instance(javacProcessingEnv);
        maker = TreeMaker.instance(javacProcessingEnv.getContext());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) return false;
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "EormProcessor processing");

        Set<? extends Element> elements = roundEnv.getRootElements();
        for (Element element : elements) {
            try {
                doProcess(element);
            } catch (Exception e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, e.getMessage(), element);
            }
        }
        return false;
    }

    private void doProcess(Element element) throws Exception {

        if (element.getKind() != ElementKind.CLASS) {
            return;
        }

        TypeElement typeElement = (TypeElement) element;
        PackageElement packageElement = elements.getPackageOf(element);
        String packageName = packageElement.getQualifiedName().toString();
        String className = typeElement.getSimpleName().toString();

        String sourceCode = null;
        try {
            FileObject fileObject = filer.getResource(StandardLocation.SOURCE_PATH, packageName, className + JavaFileObject.Kind.SOURCE.extension);
            sourceCode = fileObject.getCharContent(true).toString();
        } catch (Exception e) {
            // processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, e.getMessage(), element);
        }

        // TODO IDE build
        if (null == sourceCode) {
            FileObject fileObject = filer.getResource(StandardLocation.CLASS_OUTPUT, packageName, className + JavaFileObject.Kind.SOURCE.extension);
            String targetPath = fileObject.toUri().getPath();
            String sourcePath = targetPath.replace("target/classes", "src/main/java");
            File f = new File(sourcePath);
            FileInputStream is = new FileInputStream(f);
            byte[] bytes = new byte[is.available()];
            is.read(bytes);
            is.close();
            sourceCode = new String(bytes);
        }

        // pos, mut-line code
        final Map<Integer, String> templates = new HashMap<Integer, String>();
        for (int i = 0; i < sourceCode.length(); i++) {
            char c = sourceCode.charAt(i);
            // `""/*{` start
            if ('"' == c && '"' == sourceCode.charAt(i + 1) && '/' == sourceCode.charAt(i + 2)
                    && '*' == sourceCode.charAt(i + 3) && '{' == sourceCode.charAt(i + 4)) {
                int pos = i;
                int j = i + 5;
                StringBuilder temp = new StringBuilder();
                for (; j < sourceCode.length(); j++) {
                    // `}*/;` end
                    if ('}' == sourceCode.charAt(j) && '*' == sourceCode.charAt(j + 1)
                            && '/' == sourceCode.charAt(j + 2) && ';' == sourceCode.charAt(j + 3)) {
                        break;
                    }
                    temp.append(sourceCode.charAt(j));
                }
                templates.put(pos, temp.toString());
                i = j;
            }
        }
        if (templates.isEmpty()) {
            return;
        }

        JCTree tree = (JCTree) trees.getTree(element);
        tree.accept(new TreeTranslator() {
            @Override
            public void visitVarDef(JCTree.JCVariableDecl jcVariableDecl) {
                super.visitVarDef(jcVariableDecl);

                // String str = ""/*{code}*/
                int pos = jcVariableDecl.pos + jcVariableDecl.name.length() + 3;
                String content = templates.get(pos);
                if (null != content) {
                    jcVariableDecl.init = maker.Literal(content);
                }
            }
        });
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.values()[SourceVersion.values().length - 1];
    }
}