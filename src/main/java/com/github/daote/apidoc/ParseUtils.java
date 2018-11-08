package com.github.daote.apidoc;

import com.github.daote.apidoc.exception.JavaFileNotFoundException;
import com.github.daote.apidoc.parser.*;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.type.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.*;

/**
 * some util methods during parse
 *
 * @author yeguozhong yedaxia.github.com
 */
public class ParseUtils {

    /**
     * means a model class type
     */
    private static final String TYPE_MODEL = "unkown";

    /**
     * search File of className in the java file
     *
     * @param inJavaFile
     * @param className
     * @return
     */
    public static File searchJavaFile(File inJavaFile, String className){
       File file = null;

       for(String javaSrcPath : DocContext.getJavaSrcPaths()){
           file = searchJavaFileInner(javaSrcPath, inJavaFile, className);
           if(file != null){
               break;
           }
       }

       if(file == null){
           throw new JavaFileNotFoundException("Cannot find java file , in java file : " + inJavaFile.getAbsolutePath() + ", className : " +className);
       }

       return file;
    }

    private static File searchJavaFileInner(String javaSrcPath, File inJavaFile, String className){
        CompilationUnit compilationUnit = compilationUnit(inJavaFile);

        String[] cPaths;

        Optional<ImportDeclaration> idOp = compilationUnit.getImports()
                .stream()
                .filter(im -> im.getNameAsString().endsWith(className))
                .findFirst();

        //found in import
        if(idOp.isPresent()){
            cPaths = idOp.get().getNameAsString().split("\\.");
            return backTraceJavaFileByName(javaSrcPath, cPaths);
        }

        //inner class
        if(getInnerClassNode(compilationUnit, className).isPresent()){
            return inJavaFile;
        }

        cPaths = className.split("\\.");

        //current directory
        if(cPaths.length == 1){

            File[] javaFiles = inJavaFile.getParentFile().listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.equals(className + ".java");
                }
            });

            if(javaFiles != null && javaFiles.length == 1){
                return javaFiles[0];
            }

        }else{

            final String firstPath = cPaths[0];
            //same package inner class
            File[] javaFiles = inJavaFile.getParentFile().listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    int i = name.lastIndexOf(".java");
                    if(i == -1){
                        return false;
                    }
                    return name.substring(0, i).equals(firstPath);
                }
            });

            if(javaFiles != null && javaFiles.length > 0){
                File javaFile = javaFiles[0];
                if(getInnerClassNode(compilationUnit(javaFile), className).isPresent()){
                    return javaFile;
                }
            }
        }

        //maybe a complete class name
        File javaFile = backTraceJavaFileByName(javaSrcPath, cPaths);
        if(javaFile != null){
            return javaFile;
        }

        //.* at import
        NodeList<ImportDeclaration> importDeclarations = compilationUnit.getImports();
        if(importDeclarations.isNonEmpty()){
            for(ImportDeclaration importDeclaration : importDeclarations){
                if(importDeclaration.toString().contains(".*")){
                    String packageName = importDeclaration.getNameAsString();
                    cPaths = (packageName + "." + className).split("\\.");
                    javaFile = backTraceJavaFileByName(javaSrcPath, cPaths);
                    if(javaFile != null){
                        break;
                    }
                }
            }
        }

        return javaFile;
    }

    /**
     * get inner class node
     *
     * @param compilationUnit
     * @param className
     * @return
     */
    private static Optional<TypeDeclaration> getInnerClassNode(CompilationUnit compilationUnit , String className){
        return compilationUnit.getChildNodesByType(TypeDeclaration.class)
                .stream()
                .filter( c -> c instanceof ClassOrInterfaceDeclaration ||  c instanceof EnumDeclaration)
                .filter( c -> className.endsWith(c.getNameAsString()))
                .findFirst();
    }

    private static File backTraceJavaFileByName(String javaSrcPath, String[] cPaths){
        if(cPaths.length == 0){
            return null;
        }
        String javaFilePath = javaSrcPath + Utils.joinArrayString(cPaths, "/") +".java";
        File javaFile = new File(javaFilePath);
        if(javaFile.exists() && javaFile.isFile()){
            return javaFile;
        }else{
            return backTraceJavaFileByName(javaSrcPath, Arrays.copyOf(cPaths, cPaths.length - 1));
        }
    }

    /**
     * get java file parser object
     *
     * @param javaFile
     * @return
     */
    public static CompilationUnit compilationUnit(File javaFile){
        try{
            return JavaParser.parse(javaFile);
        }catch (FileNotFoundException e){
            throw new RuntimeException("java file not exits , file path : " + javaFile.getAbsolutePath());
        }
    }

    /**
     * parse class model java file
     *
     * @param inJavaFile
     * @param classType
     */
    public static void parseClassNodeByType(File inJavaFile, ClassNode rootClassNode, Type classType){
        if(classType.getParentNode().get() instanceof ArrayType){
            rootClassNode.setList(true);
        }else if(classType instanceof ArrayType){
            rootClassNode.setList(true);
            classType = ((ArrayType) classType).getComponentType();
        }else if(isCollectionType(classType.asString())){
            rootClassNode.setList(true);
            List<ClassOrInterfaceType> collectionType = classType.getChildNodesByType(ClassOrInterfaceType.class);
            if(collectionType.isEmpty()){
                LogUtils.warn("We found Collection without specified Class Type, Please check ! java file : %s", inJavaFile.getName());
                rootClassNode.setClassName("Object");
                return;
            }else{
                classType = collectionType.get(0);
            }
        }

        String unifyClassType = unifyType(classType.asString());
        if(TYPE_MODEL.equals(unifyClassType)){
            if(classType instanceof  ClassOrInterfaceType){

                ((ClassOrInterfaceType) classType).getTypeArguments().ifPresent(typeList->typeList.forEach(argType->{
                    GenericNode rootGenericNode = new GenericNode();
                    rootGenericNode.setFromJavaFile(inJavaFile);
                    rootGenericNode.setClassType(argType);
                    rootClassNode.addGenericNode(rootGenericNode);
                }));

                String className = ((ClassOrInterfaceType)classType).getName().getIdentifier();
                rootClassNode.setClassName(className);
                parseClassNode(searchJavaFile(inJavaFile, className), rootClassNode);
            }
        }else{
            rootClassNode.setClassName(unifyClassType);
        }
    }

    /**
     * parse class model java file
     *
     * @param modelJavaFile
     * @param classNode
     */
    public static void parseClassNode(File modelJavaFile, ClassNode classNode){
        String resultClassName = classNode.getClassName();

        ParseUtils.compilationUnit(modelJavaFile).
                getChildNodesByType(ClassOrInterfaceDeclaration.class).
                stream().filter(f -> resultClassName.endsWith(f.getNameAsString())).findFirst().ifPresent(cl -> {

                    //handle generic type
                    NodeList<TypeParameter> typeParameters = cl.getTypeParameters();
                    if(typeParameters.isNonEmpty() && classNode.getGenericNodes().size() == typeParameters.size()){
                        for(int i = 0, len = typeParameters.size(); i != len; i++){
                            classNode.getGenericNode(i).setPlaceholder(typeParameters.get(i).getName().getIdentifier());
                        }
                    }

                    NodeList<ClassOrInterfaceType> exClassTypeList =  cl.getExtendedTypes();
                    if(!exClassTypeList.isEmpty()){
                        String extendClassName = exClassTypeList.get(0).getNameAsString();
                        classNode.setClassName(extendClassName);
                        parseClassNode(ParseUtils.searchJavaFile(modelJavaFile, extendClassName), classNode);
                    }

                    cl.getChildNodesByType(FieldDeclaration.class)
                    .stream().filter(fd -> !fd.getModifiers().contains(Modifier.STATIC))
                    .forEach(fd -> {

                        //内部类字段也会读取到，这里特殊处理
                        ClassOrInterfaceDeclaration cClDeclaration = (ClassOrInterfaceDeclaration)fd.getParentNode().get();
                        if(!resultClassName.endsWith(cClDeclaration.getNameAsString())){
                            return;
                        }

                        fd.getVariables().forEach(field -> {
                            FieldNode fieldNode = new FieldNode();
                            fieldNode.setClassNode(classNode);

                            classNode.addChildNode(fieldNode);
                            fd.getComment().ifPresent(c -> fieldNode.setDescription(Utils.cleanCommentContent(c.getContent())));

                            if(!Utils.isNotEmpty(fieldNode.getDescription())){
                                field.getComment().ifPresent(c -> fieldNode.setDescription(Utils.cleanCommentContent(c.getContent())));
                            }

                            fd.getAnnotationByName("RapMock").ifPresent(an -> {
                                if(an instanceof NormalAnnotationExpr){
                                    NormalAnnotationExpr normalAnExpr = (NormalAnnotationExpr)an;
                                    MockNode mockNode = new MockNode();
                                    for(MemberValuePair mvPair : normalAnExpr.getPairs()){
                                        String name = mvPair.getName().asString();
                                        if("limit".equalsIgnoreCase(name)){
                                            mockNode.setLimit(Utils.removeQuotations(mvPair.getValue().toString()));
                                        }else if("value".equalsIgnoreCase(name)){
                                            mockNode.setValue(Utils.removeQuotations(mvPair.getValue().toString()));
                                        }
                                    }
                                    fieldNode.setMockNode(mockNode);
                                }else if(an instanceof SingleMemberAnnotationExpr){
                                    SingleMemberAnnotationExpr singleAnExpr = (SingleMemberAnnotationExpr)an;
                                    MockNode mockNode = new MockNode();
                                    mockNode.setValue(Utils.removeQuotations(singleAnExpr.getMemberValue().toString()));
                                    fieldNode.setMockNode(mockNode);
                                }
                            });

                            fieldNode.setName(field.getNameAsString());

                            Type fieldType = fd.getElementType();
//                            if(fieldNode.getName().contains("children"))
//                                System.out.println(fieldNode.getName());

                            parseFieldNode(fieldNode, modelJavaFile, fieldType,resultClassName);
                        });
                    });
        });

        //恢复原来的名称
        classNode.setClassName(resultClassName);
    }

    private static void parseFieldNode(FieldNode fieldNode, File inJavaFile, Type fieldType,String fileClassName){
        final GenericNode genericNode = fieldNode.getClassNode().getGenericNode(fieldType.asString());

        //Enum
        if(genericNode == null && !fieldType.asString().contains("<")){
            final String fieldClassType = fieldType.asString();
            if(TYPE_MODEL.equals(unifyType(fieldClassType))){

                Optional<EnumDeclaration> ed = null;
                try{
                    File childJavaFile = searchJavaFile(inJavaFile, fieldClassType);
                    ed = compilationUnit(childJavaFile)
                            .getChildNodesByType(EnumDeclaration.class)
                            .stream()
                            .filter( em -> fieldClassType.endsWith(em.getNameAsString()))
                            .findFirst();
                }catch (JavaFileNotFoundException ex){
                    LogUtils.info("we think %s should not be an enum type", fieldClassType);
                }

                if(ed != null && ed.isPresent()){
                    fieldNode.setType("string");
                    List<EnumConstantDeclaration> constants = ed.get().getChildNodesByType(EnumConstantDeclaration.class);
                    StringBuilder sb = new StringBuilder(fieldNode.getDescription() == null ? "" : fieldNode.getDescription());
                    sb.append(" [");
                    for(int i = 0 , size = constants.size(); i != size ; i++){
                        sb.append(constants.get(i).getNameAsString());
                        if(i != size -1){
                            sb.append(",");
                        }
                    }
                    sb.append("]");
                    fieldNode.setDescription(sb.toString());
                    return;
                }
            }
        }

        // generic type field, do replacement
        if(genericNode != null){
            fieldType = genericNode.getClassType();
            inJavaFile = genericNode.getFromJavaFile();
        }

        Boolean isList;

        if(fieldType.getParentNode().get() instanceof ArrayType){
            isList = true;
        }else if(fieldType instanceof ArrayType){
            isList = true;
            fieldType = ((ArrayType) fieldType).getComponentType();
            GenericNode arrayTypeNode = fieldNode.getClassNode().getGenericNode(fieldType.asString());
            if(arrayTypeNode != null){
                fieldType = arrayTypeNode.getClassType();
                inJavaFile = arrayTypeNode.getFromJavaFile();
            }
        }else{
            if(isCollectionType(fieldType.asString())){

                isList = true;
                List<ClassOrInterfaceType> collectionType = fieldType.getChildNodesByType(ClassOrInterfaceType.class);
                if(collectionType.isEmpty()){
                    LogUtils.warn("We found Collection without specified Class Type, Please check ! java file : %s", inJavaFile.getName());
                    fieldNode.setType("Object[]");
                    return;
                }else{
                    // 是否在泛型列表中
                    GenericNode collectionGenericNode = fieldNode.getClassNode().getGenericNode(collectionType.get(0).asString());
                    if(collectionGenericNode != null){
                        fieldType = collectionGenericNode.getClassType();
                        inJavaFile = collectionGenericNode.getFromJavaFile();
                    }else{
                        fieldType = collectionType.get(0);
                    }
                }
            }else{
                isList = false;
            }
        }


        String fieldClassType;

        if(fieldType instanceof ClassOrInterfaceType){
            fieldClassType = ((ClassOrInterfaceType) fieldType).getName().getIdentifier();
        }else{
            fieldClassType = fieldType.asString();
        }

        final String unifyType = unifyType(fieldClassType);

        if(TYPE_MODEL.equals(unifyType)){

            ResponseNode childClassNode = new ResponseNode();
            fieldNode.setChildResponseNode(childClassNode);
            childClassNode.setList(isList);
            fieldNode.setType(isList ? fieldClassType + "[]" : fieldClassType);
            childClassNode.setClassName(fieldClassType);

            final File childJavaFile = inJavaFile;
            ((ClassOrInterfaceType)fieldType).getTypeArguments().ifPresent(typeList->typeList.forEach(argType->{
                GenericNode childClassGenericNode = new GenericNode();

                if(argType instanceof ArrayType){
                    GenericNode arrayTypeNode = fieldNode.getClassNode().getGenericNode(((ArrayType) argType).getComponentType().asString());
                    if(arrayTypeNode != null){
                        ((ArrayType) argType).setComponentType(arrayTypeNode.getClassType());
                    }
                }else{
                    GenericNode arrayTypeNode = fieldNode.getClassNode().getGenericNode(argType.asString());
                    if(arrayTypeNode != null){
                        argType = arrayTypeNode.getClassType();
                    }
                }

                childClassGenericNode.setClassType(argType);
                childClassGenericNode.setFromJavaFile(childJavaFile);
                childClassNode.addGenericNode(childClassGenericNode);
            }));

            try{
                File childNodeJavaFile = searchJavaFile(inJavaFile, fieldClassType);
                //修改，以防止自身类型无限递归
                if(!childClassNode.getClassName().equals(fileClassName))
                     parseClassNode(childNodeJavaFile, childClassNode);
            }catch (JavaFileNotFoundException ex){
                LogUtils.warn(ex.getMessage()+", we cannot found more information of it, you've better to make it a JavaBean");
                fieldNode.setType(isList? "Object[]": "Object");
            }
        } else {
            fieldNode.setType(isList ? unifyType + "[]" : unifyType);
        }
    }

    /**
     * is model type or not
     * @param className
     * @return
     */
    public static boolean isModelType(String className){
        return TYPE_MODEL.equals(unifyType(className));
    }

    /**
     * unify the type show in docs
     *
     * @param className
     * @return
     */
    public static String unifyType(String className){
        String[] cPaths = className.split("\\.");
        String rawType = cPaths[cPaths.length - 1];
        if("byte".equalsIgnoreCase(rawType)){
            return "byte";
        } else if("short".equalsIgnoreCase(rawType)){
            return "short";
        } else if("int".equalsIgnoreCase(rawType)
                || "Integer".equalsIgnoreCase(rawType)
                || "BigInteger".equalsIgnoreCase(rawType)){
            return "int";
        } else if("long".equalsIgnoreCase(rawType)){
            return "long";
        } else if("float".equalsIgnoreCase(rawType)){
            return "float";
        } else if("double".equalsIgnoreCase(rawType)
                ||"BigDecimal".equalsIgnoreCase(rawType)){
            return "double";
        } else if("boolean".equalsIgnoreCase(rawType)){
            return "boolean";
        } else if("char".equalsIgnoreCase(rawType)
                || "Character".equalsIgnoreCase(rawType)){
            return "char";
        }else if("String".equalsIgnoreCase(rawType)){
            return "string";
        } else if("date".equalsIgnoreCase(rawType)
                || "ZonedDateTime".equalsIgnoreCase(rawType)){
            return "date";
        } else if("file".equalsIgnoreCase(rawType)){
            return "file";
        } else{
            return TYPE_MODEL;
        }
    }

    /**
     *  is implements from Collection or not
     *
     * @param className
     * @return
     */
    public static boolean isCollectionType(String className){
        String[] cPaths = className.split("\\.");
        String genericType = cPaths[cPaths.length - 1];
        int genericLeftIndex = genericType.indexOf("<");
        String rawType = genericLeftIndex != -1 ? genericType.substring(0, genericLeftIndex) : genericType;
        String collectionClassName = "java.util."+rawType;
        try{
            Class collectionClass = Class.forName(collectionClassName);
            return Collection.class.isAssignableFrom(collectionClass);
        }catch (ClassNotFoundException e){
            return false;
        }
    }
}
