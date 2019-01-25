package com.marksmile.icp.tools;

import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.core.JFinal;
import com.jfinal.template.Engine;
import com.jfinal.template.source.FileSourceFactory;
import com.marksmile.icp.tools.controllers.IndexController;

public class MyJFinalConfig extends JFinalConfig {
    public void configConstant(Constants me) {
        me.setDevMode(false);
    }

    public void configRoute(Routes me) {
        me.add("/", IndexController.class);
    }

    public void configEngine(Engine me) {
        me.setDevMode(true);
        me.setBaseTemplatePath("webview");
        me.setSourceFactory(new FileSourceFactory());
//        me.addSharedFunction("/js_css.html");
//        me.addSharedFunction("/page.html");
    }

    public void configPlugin(Plugins me) {
    }

    public void configInterceptor(Interceptors me) {
//    	me.addGlobalActionInterceptor(new InterceptorsTest());
    }

    public void configHandler(Handlers me) {
    }

    public static void main(String[] args) {
        JFinal.main(new String[] { "webapp", "80", "/", "-1" });
    }
}
