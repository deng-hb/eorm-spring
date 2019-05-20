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
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.InputStream;
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

        elements = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();

        trees = Trees.instance(javacProcessingEnv);
        maker = TreeMaker.instance(javacProcessingEnv.getContext());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) return true;
        try {
            Set<? extends Element> elements = roundEnv.getRootElements();
            for (Element element : elements) {
                doProcess(element);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private void doProcess(Element element) throws IOException {

        if (element.getKind() != ElementKind.CLASS) {
            return;
        }

        TypeElement typeElement = (TypeElement) element;
        PackageElement packageElement = elements.getPackageOf(element);
        String packageName = packageElement.getQualifiedName().toString();
        String className = typeElement.getSimpleName().toString();

        FileObject fileObject = filer.
                getResource(StandardLocation.SOURCE_PATH, packageName, className + JavaFileObject.Kind.SOURCE.extension);

        InputStream is = fileObject.openInputStream();
        byte[] bytes = new byte[is.available()];
        is.read(bytes);
        is.close();

        String str = new String(bytes);

        // pos, mut-line code
        final Map<Integer, String> templates = new HashMap<Integer, String>();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            // `""/*{` start
            if ('"' == c && '"' == str.charAt(i + 1) && '/' == str.charAt(i + 2)
                    && '*' == str.charAt(i + 3) && '{' == str.charAt(i + 4)) {
                int pos = i;
                int j = i + 5;
                StringBuffer sb = new StringBuffer();
                for (; j < str.length(); j++) {
                    // `}*/;` end
                    if ('}' == str.charAt(j) && '*' == str.charAt(j + 1)
                            && '/' == str.charAt(j + 2) && ';' == str.charAt(j + 3)) {
                        break;
                    }
                    sb.append(str.charAt(j));
                }
                templates.put(pos, sb.toString());
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
