package com.ajaxjs.embeded_tomcat;

import com.ajaxjs.util.StrUtil;
import com.ajaxjs.util.io.FileHelper;
import com.ajaxjs.util.io.Resources;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.WebResourceRoot.ResourceSetType;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.coyote.AbstractProtocol;
import org.apache.coyote.ProtocolHandler;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.apache.tomcat.util.descriptor.web.JspPropertyGroup;
import org.apache.tomcat.util.descriptor.web.JspPropertyGroupDescriptorImpl;
import org.apache.tomcat.util.scan.StandardJarScanFilter;
import org.apache.catalina.*;

import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.servlet.Filter;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.registry.LocateRegistry;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Tomcat 的功能
 */
@Slf4j
public class TomcatStarter {
    /**
     * 创建一个 TomcatStarter
     *
     * @param cfg 配置
     */
    public TomcatStarter(TomcatConfig cfg) {
        this.cfg = cfg;
    }

    /**
     * 自定义配置
     */
    public TomcatConfig cfg;

    /**
     * Tomcat 对象
     */
    Tomcat tomcat;

    /**
     * 获取监控信息用
     */
    public static Tomcat TOMCAT;

    /**
     * Tomcat 上下文对象
     */
    Context context;

    /**
     * 记录启动时间
     */
    public static long startedTime;

    /**
     * 记录 Spring 启动时间
     */
    public static long springTime;

    /**
     * 启动 Tomcat
     */
    public void start() {
        startedTime = System.currentTimeMillis();
        initTomcat();
        initConnector();
        initContext();
        runTomcat();
    }

    /**
     * 初始化 Tomcat 对象
     */
    private void initTomcat() {
        tomcat = new Tomcat();
        String tomcatBaseDir = cfg.getBaseDir();

        if (tomcatBaseDir == null)
            tomcatBaseDir = System.getProperty("java.io.tmpdir") + "tomcat_embed_works_tmpdir";
//            tomcatBaseDir = TomcatUtil.createTempDir("tomcat_embed_works_tmpdir").getAbsolutePath();

        tomcat.setBaseDir(tomcatBaseDir);
        tomcat.setPort(cfg.getPort());
        tomcat.setHostname(cfg.getHostName());
        tomcat.enableNaming();

        TOMCAT = tomcat;
    }

    /**
     * 启动 Tomcat
     */
    private void runTomcat() {
        try {
            tomcat.start(); // tomcat 启动
        } catch (LifecycleException e) {
            log.warn("ERROR>>", e);
            throw new RuntimeException(e);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                log.info("关闭 Tomcat");
                tomcat.destroy();
            } catch (LifecycleException e) {
                log.warn("ERROR>>", e);
            }
        }));

        String tpl = "Web 服务启动完毕。Spring 耗时：%sms，总耗时：%sms 127.0.0.1:" + cfg.getPort() + cfg.getContextPath();
        tpl = String.format(tpl, springTime, System.currentTimeMillis() - startedTime);
        log.info(tpl);

        // 注册关闭端口以进行关闭
        // 可以通过Socket关闭tomcat： telnet 127.0.0.1 8005，输入SHUTDOWN字符串
//        tomcat.getServer().setPort(cfg.getShutdownPort());
        tomcat.getServer().await(); // 保持主线程不退出，让其阻塞，不让当前线程结束，等待处理请求
        log.info("正在关闭 Tomcat，shutdown......");

        try {
            tomcat.stop();
        } catch (LifecycleException e) {
            log.warn("ERROR>>", e);
        }

        // 删除 tomcat 临时路径
//        TomcatUtil.deleteAllFilesOfDir(tomcatBaseDirFile);
    }

    /**
     * 获取正在运行的 JAR 文件的目录
     * 如果您在 IDE 中运行代码，则该代码可能会返回项目的根目录
     *
     * @return JAR 文件的目录
     */
    public static String getJarDir() {
        String jarDir = null;

        try {
            jarDir = new File(TomcatStarter.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent();
        } catch (URISyntaxException e) {
            log.warn("ERROR>>", e);
        }

        return jarDir;
    }

    /**
     * 读取项目路径
     */
    private void initContext() {
        String jspFolder = cfg.getDocBase();

//        if (jspFolder == null)
//            jspFolder = Resources.getResourcesFromClasspath("META-INF\\resources");// 开放调试阶段，直接读取源码的

        if (jspFolder == null) {
            jspFolder = getJarDir() + "/../webapp"; // 部署阶段。这个并不会实际保存 jsp。因为 jsp 都在 META-INF/resources 里面。但因为下面的 addWebapp() 又需要
            FileHelper.mkDir(jspFolder);
        }

//        System.out.println("jspFolder::::::" + jspFolder);
//        StandardContext ctx = (StandardContext) tomcat.addWebapp("/", new File("/mycar/mycar-service-4.0/security-oauth2-uam/sync/jsp").getAbsolutePath());
        Host host = tomcat.getHost();
        host.setAutoDeploy(false);
        host.setAppBase("webapp");

        context = tomcat.addWebapp(host, cfg.getContextPath(), jspFolder, (LifecycleListener) new EmbededContextConfig());
        context.setReloadable(cfg.getEnableJsp());// 设置 JSP 文件的自动编译属性
        context.addLifecycleListener(new Tomcat.FixContextListener());// required if you don't use web.xml

        onContextReady(context);

        // seems not work
        WebResourceRoot resources = new StandardRoot(context);// 创建 WebRoot
        String classDir = new File("target/classes").getAbsolutePath();
        resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes", classDir, "/"));// tomcat 内部读取 Class 执行

        if (cfg.getEnableSsi())
            ssi();

        if (cfg.getEnableJsp())
            initJSP();
//        else
//            disableJsp();

//        context.setJarScanner(new EmbeddedStandardJarScanner());
//        context.setParentClassLoader(TomcatStarter.class.getClassLoader());// needs?
        addWebXmlMountListener();
        setTomcatDisableScan();
//        initFilterByTomcat(UTF8CharsetFilter.class);
    }

    /**
     * <a href="https://blog.csdn.net/hundan_520520/article/details/103483041">...</a>
     * <a href="http://www.ice-maple.com/2019/12/06/springboot%E9%85%8D%E7%BD%AEjsp-config%E7%9A%84%E6%8E%A2%E7%A9%B6/">...</a>
     */
    private void initJSP() {
        JspPropertyGroup jspPropertyGroup = new JspPropertyGroup();
        jspPropertyGroup.setElIgnored("false");
        jspPropertyGroup.addUrlPattern("*.jsp");
        jspPropertyGroup.setPageEncoding(StrUtil.UTF8_SYMBOL);

        // 项目启动时，可以在每个 JSP 页面上加入 tagelibs.jspf 的头文件
//        jspPropertyGroup.addIncludePrelude("/WEB-INF/tags/taglibs.jspf");
        JspPropertyGroupDescriptorImpl jspPropertyGroupDescriptor = new JspPropertyGroupDescriptorImpl(jspPropertyGroup);
        // jsp-property-group列表和taglib列表
//        context.setJspConfigDescriptor(new JspConfigDescriptorImpl(Collections.singletonList(jspPropertyGroupDescriptor), Collections.emptyList()));
//        context.addLifecycleListener(new org.apache.catalina.core.JasperListener());
    }

    /**
     * 当上下文准备就绪时调用此方法。
     * <p>
     * 您可以通过覆盖此方法来在此处执行初始化操作或访问上下文。
     * 建议避免进行重型操作以确保应用的响应性。
     *
     * @param context 应用上下文，可用于访问全局应用信息
     */
    public void onContextReady(Context context) {
    }

    /**
     * 禁止 Tomcat 自动扫描 jar 包，那样会很慢
     */
    private void setTomcatDisableScan() {
        StandardJarScanFilter filter = (StandardJarScanFilter) context.getJarScanner().getJarScanFilter();
        filter.setDefaultTldScan(false);

        /*
         * 这个对启动 tomcat 时间影响很大 又 很多 Servlet 3.0 新特性，不能禁掉，比如在 jar 里面放
         * jsp（部署时候就会这样，但开放阶段不用）。 故，用 isDebug 判断下
         */

        filter.setDefaultPluggabilityScan(cfg.getEnableJsp());
//        if (Version.isDebug)
//            filter.setDefaultPluggabilityScan(false);
//      String oldTldSkip = filter.getTldSkip();
//      System.out.println("-------" + oldTldSkip);
//      String newTldSkip = oldTldSkip == null || oldTldSkip.trim().isEmpty() ? "pdq.jar" : oldTldSkip + ",pdq.jar";
//      filter.setTldSkip(newTldSkip);
    }

    /**
     * 设置 Connector
     */
    void initConnector() {
        Connector connector;

        if (cfg.isCustomerConnector()) {// 创建连接器，并且添加对应的连接器，同时连接器指定端口 设置 IO 协议
            connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
            connector.setPort(cfg.getPort());
            connector.setThrowOnFailure(true);

            tomcat.getService().addConnector(connector);// 只能设置一个 service,直接拿默认的
            tomcat.setConnector(connector); // 设置执行器
        } else
            connector = tomcat.getConnector();

        connector.setURIEncoding(StrUtil.UTF8_SYMBOL); // 设置 URI 编码支持中文

        ProtocolHandler handler = connector.getProtocolHandler();

        // 设置 Tomcat 配置
        if (handler instanceof AbstractProtocol) {
            AbstractProtocol<?> protocol = (AbstractProtocol<?>) handler;

            if (cfg.getMinSpareThreads() > 0)
                protocol.setMinSpareThreads(cfg.getMinSpareThreads());

            if (cfg.getMaxThreads() > 0)
                protocol.setMaxThreads(cfg.getMaxThreads());

            if (cfg.getConnectionTimeout() > 0)
                protocol.setConnectionTimeout(cfg.getConnectionTimeout());

            if (cfg.getMaxConnections() > 0)
                protocol.setMaxConnections(cfg.getMaxConnections());

            if (cfg.getAcceptCount() > 0)
                protocol.setAcceptCount(cfg.getAcceptCount());
        }

        // Tomcat 的 startStopThreads 属性用于配置 Tomcat 服务器启动和关闭时的线程池大小。它决定了 Tomcat 在启动和关闭过程中能够同时处理的任务数。
        // 对于 Tomcat 8，没有直接的编程方式来设置 startStopThreads 属性
        Executor executor = handler.getExecutor();

        if (executor instanceof ThreadPoolExecutor) {// doesn't work
            ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;
            threadPoolExecutor.setCorePoolSize(3);// 设置核心线程数和最大线程数
            threadPoolExecutor.setMaximumPoolSize(3);
        }

        if (cfg.isEnableJMX()) {
            Connector jmxConnector = new Connector("org.apache.coyote.jmx.JmxProtocol");
            jmxConnector.setPort(8999); // Set the desired JMX port
            tomcat.getService().addConnector(jmxConnector);
        }
    }

    /**
     * context load WEB-INF/web.xml from classpath
     */
    void addWebXmlMountListener() {
        context.addLifecycleListener(event -> {
            if (event.getType().equals(Lifecycle.BEFORE_START_EVENT)) {
                Context context = (Context) event.getLifecycle();
                WebResourceRoot resources = context.getResources();

                if (resources == null) {
                    resources = new StandardRoot(context);
                    context.setResources(resources);
                }

                /*
                 * When run as embedded tomcat, context.getParentClassLoader() is AppClassLoader,so it can load "WEB-INF/web.xml" from app classpath.
                 */
                URL resource = context.getParentClassLoader().getResource("WEB-INF/web.xml");

                if (resource != null) {
                    String webXmlUrlString = resource.toString();

                    try {
                        URL root = new URL(webXmlUrlString.substring(0, webXmlUrlString.length() - "WEB-INF/web.xml".length()));
                        resources.createWebResourceSet(ResourceSetType.RESOURCE_JAR, "/WEB-INF", root, "/WEB-INF");
                    } catch (MalformedURLException e) {
                        log.warn("ERROR>>", e);
                    }
                }
            }
        });
    }

    /**
     * 禁用 JSP
     */
    void disableJsp() {
        LifecycleListener tmplf = null;

        for (LifecycleListener lfl : context.findLifecycleListeners()) {
            if (lfl instanceof Tomcat.DefaultWebXmlListener) {
                tmplf = lfl;
                break;
            }
        }

        if (tmplf != null)
            context.removeLifecycleListener(tmplf);

        context.addLifecycleListener(event -> {
            if (Lifecycle.BEFORE_START_EVENT.equals(event.getType())) {
                Context context = (Context) event.getLifecycle();
                Tomcat.initWebappDefaults(context);
                // 去掉JSP
                context.removeServletMapping("*.jsp");
                context.removeServletMapping("*.jspx");
                context.removeChild(context.findChild("jsp"));
            }
        });
    }

    /**
     * 在 Tomcat 初始化阶段设置 Filter
     */
    @SuppressWarnings("unused")
    private void initFilterByTomcat(Class<? extends Filter> filterClz) {
        FilterDef filter1definition = new FilterDef();
        filter1definition.setFilterName(filterClz.getSimpleName());
        filter1definition.setFilterClass(filterClz.getName());
        context.addFilterDef(filter1definition);

        FilterMap filter1mapping = new FilterMap();
        filter1mapping.setFilterName(filterClz.getSimpleName());
        filter1mapping.addURLPattern("/*");
        context.addFilterMap(filter1mapping);
    }

    /**
     * 将定义好的 Tomcat MBean 注册到 MBeanServer
     * 参见 <a href="https://blog.csdn.net/zhangxin09/article/details/132136748">...</a>
     */
    protected static void connectMBeanServer() {
        try {
            LocateRegistry.createRegistry(9011); //这个步骤很重要，注册一个端口，绑定url  后用于客户端通过 rmi 方式连接 JMXConnectorServer
            JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(new JMXServiceURL("service:jmx:rmi://localhost/jndi/rmi://localhost:9011/jmxrmi"), null, ManagementFactory.getPlatformMBeanServer() // 获取当前 JVM 的 MBeanServer，ObjectName 是 MBean 的唯一标示，一个 MBeanServer 不能有重复。
                    // 完整的格式「自定义命名空间:type=自定义类型,name=自定义名称」。当然你可以只声明 type ，不声明 name
            );

            cs.start();
            log.info("成功启动 JMXConnectorServer");
        } catch (IOException e) {
            log.warn("ERROR>>", e);
        }
    }

    /**
     * SSI（服务器端嵌入）
     */
    void ssi() {
        context.setPrivileged(true);
        Wrapper servlet = Tomcat.addServlet(context, "ssi", "org.apache.catalina.ssi.SSIServlet");
        servlet.addInitParameter("buffered", "1");
        servlet.addInitParameter("inputEncoding", StrUtil.UTF8_SYMBOL);
        servlet.addInitParameter("outputEncoding", StrUtil.UTF8_SYMBOL);
        servlet.addInitParameter("debug", "0");
        servlet.addInitParameter("expires", "666");
        servlet.addInitParameter("isVirtualWebappRelative", "4");
        servlet.setLoadOnStartup(4);
        servlet.setOverridable(true);

        // Servlet mappings
        context.addServletMappingDecoded("*.html", "ssi");
        context.addServletMappingDecoded("*.shtml", "ssi");
    }
}