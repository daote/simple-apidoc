package com.github.daote;

import static org.junit.Assert.assertTrue;

import com.github.daote.apidoc.Docs;
import com.github.daote.apidoc.Resources;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }


    @Test
    public void test_generateLhtaskDocs(){
        //Resources.setDebug();
        String projectPath="D:\\Work\\Java\\IDEAWorkSpace\\lhtask";
        String docsPath="D:\\Work\\Java\\IDEAWorkSpace\\lhtask\\src\\main\\resources\\public\\api";
        Docs.DocsConfig config = new Docs.DocsConfig();
        config.setProjectPath(projectPath);
        config.setDocsPath(docsPath);
        config.setTitle("公共业务中心文档");
        Docs.buildHtmlDocs(config);
    }

    @Test
    public void test_generateDemoDocs(){
        Resources.setDebug();
        String projectPath="D:\\Work\\Java\\IDEAWorkSpace\\lhtask";
        String docsPath="D:\\doc\\out";
        Docs.DocsConfig config = new Docs.DocsConfig();
        config.setProjectPath(projectPath);
        config.setDocsPath(docsPath);
        config.setTitle("测试文档");
        Docs.buildHtmlDocs(config);
    }



    @Test
    public void test_generateHzzDocs(){
        Resources.setDebug();
        String projectPath="D:\\Work\\Java\\IDEAWorkSpace\\lhbusiness-hzz";
        String docsPath="D:\\Work\\Java\\IDEAWorkSpace\\lhbusiness-hzz\\src\\main\\resources\\public\\webmvc\\api";
        Docs.DocsConfig config = new Docs.DocsConfig();
        config.setProjectPath(projectPath);
        config.setDocsPath(docsPath);
        config.setDocsPath(docsPath);
        config.setTitle("河长制业务中心文档");

        Docs.buildHtmlDocs(config);
    }

    @Test
    public void test_generateSlfhDocs(){
        Resources.setDebug();
        String projectPath="D:\\Work\\Java\\IDEAWorkSpace\\lhbusiness-slfh";
        String docsPath="D:\\Work\\Java\\IDEAWorkSpace\\lhbusiness-slfh\\src\\main\\resources\\public\\api";
        Docs.DocsConfig config = new Docs.DocsConfig();
        config.setProjectPath(projectPath);
        config.setTitle("林火业务中心文档");
        config.setDocsPath(docsPath);
        config.setDocsPath(docsPath);
        Docs.buildHtmlDocs(config);
    }

    @Test
    public void test_generateExampleDocs(){
        Resources.setDebug();
        String projectPath="D:\\Work\\Java\\IDEAWorkSpace\\SpringBootBucket\\simple-example";
        String docsPath="D:\\Work\\Java\\IDEAWorkSpace\\SpringBootBucket\\simple-example\\src\\main\\resources\\public\\api";
        Docs.DocsConfig config = new Docs.DocsConfig();
        config.setProjectPath(projectPath);
        config.setTitle("API示例文档");
        config.setDocsPath(docsPath);
        config.setDocsPath(docsPath);
        Docs.buildHtmlDocs(config);
    }

}
