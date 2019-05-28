package com.kangjj.butterknife.complier;

import com.google.auto.service.AutoService;
import com.kangjj.annotation.BindView;
import com.kangjj.annotation.OnClick;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;

//通过auto-service中的@AutoService可以自动生成，AutoService注解处理器是Google开发的
@AutoService(Processor.class)
public class ButterKnifeProcess extends AbstractProcessor {
    private Elements elementsUtils;     //Elements中包含用于操作Element的工具方法
    private Filer filer;                //Filer用来创建新的源文件，class文件以及辅助文件

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementsUtils =  processingEnvironment.getElementUtils();
        filer =processingEnvironment.getFiler();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(BindView.class.getCanonicalName());
        types.add(OnClick.class.getCanonicalName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        //返回此注释Processor 支持的最新源版本，该方法可以通过注解@SupportedSourceVersion指定
        return SourceVersion.latestSupported();
    }

    //注解处理器的核心方法，处理具体的注解，生成Java文件
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        System.out.println("开始******************************");
        System.out.println("所有带BindView注解属性*************************");
        //获取MainActivity中所有带BindView注解的属性
        Set<? extends Element> bindViewSet = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        //保存键值对，key是com.kangjj.butterknife.MainActivity value是所有带BindView注解的属性集合
        Map<String,List<VariableElement>> bindViewMap = new HashMap<>();
        for (Element element : bindViewSet) {
            //转成原始属性元素（结构体元素)
            VariableElement variableElement = (VariableElement)element;
            //通过属性元素获取它所属的MainActivity类名，如com.kangjj.butterknife.MainActivity
            String activityName = getActivityName(variableElement);
            //从缓存集合中获取MainActivity所有带BindView注解的属性集合
            List<VariableElement> list = bindViewMap.get(activityName);
            if(list==null){
                list = new ArrayList<>();
                bindViewMap.put(activityName,list);
            }
            //将MainActivity所有带BindView注解的属性加入到list集合
            list.add(variableElement);
            //测试打印：每个属性的名字
            System.out.println("variableElement >>> " + variableElement.getSimpleName().toString());
        }
        System.out.println("所有带OnClick注解的方法 ----------------------------------->");
        // 获取MainActivity中所有带OnClick注解的方法
        Set<? extends Element> onClickSet = roundEnvironment.getElementsAnnotatedWith(OnClick.class);
        //保存键值对key是com.kangjj.butterknife.MainActivity value是所有带OnClick注解的属性集合
        Map<String,List<ExecutableElement>> onClickMap = new HashMap<>();
        //遍历所有带OnClick注解的方法
        for (Element element : onClickSet) {
            //转成原始属性元素（结构体元素）
            ExecutableElement executableElement = (ExecutableElement)element;
            // 通过属性元素获取它所属的MainActivity类名，如：com.kangjj.butterknife.MainActivity
            String activityName = getActivityName(executableElement);
            //从缓存集合中获取MainActivity所有带OnClick注解的方法集合
            List<ExecutableElement> list = onClickMap.get(activityName);
            if(list == null){
                list = new ArrayList<>();
                // 先加入map集合，引用变量可以动态改变值
                onClickMap.put(activityName,list);
            }
            // 将MainActivity所有带OnClick注解的方法加入到list集合
            list.add(executableElement);
            // 测试打印：每个方法的名字
            System.out.println("executableElement >>> " + executableElement.getSimpleName().toString());
        }
        //******************************开始过程**********************************
        //获取Activity完整的字符串类名（包名+类名）
        for (String activityName : bindViewMap.keySet()) {
            //获取"com.kangjj.butterknife.MainActivity"中所有空间属性的集合
            List<VariableElement> cacheElements = bindViewMap.get(activityName);
            List<ExecutableElement> clickElements = onClickMap.get(activityName);
            Writer writer = null;
            try {
                //创建一个新的源文件（Class），并返回一个对象以允许写入它
                JavaFileObject javaFileObject = filer.createSourceFile(activityName+"$ViewBinder");
                //通过属性标签获取包名标签（任意一个属性标签的父节点都是同一个包名）
                String packageName = getPackageName(cacheElements.get(0));
                //定义Writer对象，开始造币过程
                writer = javaFileObject.openWriter();
                //类名：MainActivity￥Binder，不是包名+类名$ViewBinder
                String activitySimpleName = cacheElements.get(0).getEnclosingElement().getSimpleName().toString()+"$ViewBinder";
                System.out.println("activityName >>> " + activityName + "\nactivitySimpleName >>> " + activitySimpleName);

                System.out.println("开始造币 ----------------------------------->");
                // 第一行生成包
                writer.write("package "+packageName+";\n");
                // 第二行生成要导入的接口类（必须手动导入）
                writer.write("import com.kangjj.butterknife.library.ViewBinder;\n");
                writer.write("import com.kangjj.butterknife.library.DebouncingOnClickListener;\n");
                writer.write("import android.view.View;\n");
                // 第三行生成类
                writer.write("public class "+activitySimpleName + " implements ViewBinder<"+activityName+">{\n");
                //第四行生成bind方法
                writer.write("public void bind(final " + activityName + " target) {\n");
                System.out.println("每个控件属性 ----------------------------------->");
                //循环生成MainActivity每个控件属性
                for (VariableElement variableElement : cacheElements) {
                    //控件属性名
                    String fieldName = variableElement.getSimpleName().toString();
                    //获取控件的注解
                    BindView bindView = variableElement.getAnnotation(BindView.class);
                    //获取控件注解的id值
                    int id = bindView.value();
                    //生成：target.tv = target.findViewById（xxx);
                    writer.write("target."+ fieldName +" = "+"target.findViewById("+id+");\n");
                }
                System.out.println("每个点击事件 ----------------------------------->");
                // 循环生成MainActivity每个点击事件
                for (ExecutableElement executableElement : clickElements) {
                    String methodName = executableElement.getSimpleName().toString();
                    OnClick onClick = executableElement.getAnnotation(OnClick.class);
                    int id = onClick.value();//TODO int[]
                    List<? extends VariableElement> paramters = executableElement.getParameters();
                    // 生成点击事件
                    writer.write("target.findViewById(" + id + ").setOnClickListener(new DebouncingOnClickListener() {\n");
                    writer.write("public void doClick(View view) {\n");
                    if(paramters.isEmpty()){
                        writer.write("target." + methodName + "();\n}\n});\n");
                    }else{
                        writer.write("target." + methodName + "(view);\n}\n});\n");
                    }
                }
                writer.write("\n}\n}");
                System.out.println("结束 ----------------------------------->");
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        return false;
    }

    /**
     * 通过属性标签获取类名标签，再通过类名标签获取报名标签
     * @param variableElement
     * @return  com.kangjj.butterknife.MainActivity(包名+类名）
     */
    private String getActivityName(VariableElement variableElement) {
        //通过属性标签获取类名标签，再通过类名标签获取包名标签
        String packageName = getPackageName(variableElement);
        //通过属性标签获取包名标签
        TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
        return packageName+"."+typeElement.getSimpleName().toString();
    }

    /**
     * 通过属性标签获取类名标签，再通过雷鸣标签获取包名标签（通过属性节点，找到父节点、再找到父节点的父节点）
     * @param variableElement
     * @return
     */
    private String getPackageName(VariableElement variableElement) {
        //通过属性标签获取类名标签
        TypeElement typeElement = (TypeElement)variableElement.getEnclosingElement();
        //铜鼓类名标签获取包名标签
        String packageName = elementsUtils.getPackageOf(typeElement).getQualifiedName().toString();
        System.out.println("packageName >>> "+packageName);
        return packageName;

    }

    private String getActivityName(ExecutableElement executableElement) {
        // 通过方法标签获取类名标签，再通过类名标签获取包名标签
        String packageName = getPackageName(executableElement);
        //通过方法标签获取包名标签
        TypeElement typeElement = (TypeElement) executableElement.getEnclosingElement();
        return packageName+"."+typeElement.getSimpleName().toString();
    }

    private String getPackageName(ExecutableElement executableElement) {
        //通过方法标签获取类名标签
        TypeElement typeElement = (TypeElement)executableElement.getEnclosingElement();
        //通过类名标签获取包名标签
        String packageName = elementsUtils.getPackageOf(typeElement).getQualifiedName().toString();
        System.out.println("packageName >>> "+packageName);
        return packageName;
    }
}
