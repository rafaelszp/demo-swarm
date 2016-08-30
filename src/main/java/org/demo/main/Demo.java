package org.demo.main;

import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.config.logging.Level;
import org.wildfly.swarm.datasources.DatasourcesFraction;
import org.wildfly.swarm.logging.LoggingFraction;
import org.wildfly.swarm.undertow.UndertowFraction;
import org.wildfly.swarm.undertow.WARArchive;
import org.wildfly.swarm.webservices.WebServicesFraction;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rafael on 8/23/16.
 */
public class Demo {

    static Logger logger;

    public static void main(String[] args) throws Exception{
        ClassLoader cl = Demo.class.getClassLoader();
        URL stageConfig = cl.getResource("project-stages.yml");
        Swarm swarm = new Swarm(false).withStageConfig(stageConfig);
        logger = Logger.getLogger(Demo.class);
        swarm.fraction(datasource(swarm));
        swarm.fraction(logging());
        swarm.start();
        swarm.deploy(archive());
    }

    private static WARArchive archive() throws Exception {

        WARArchive archive = ShrinkWrap.create( WARArchive.class );


        archive.addPackages(true,"org.demo");
        archive.addAsWebInfResource(new File("src/main/webapp/WEB-INF/beans.xml"));
        archive.addAsWebInfResource(new ClassLoaderAsset("META-INF/persistence.xml",
                Demo.class.getClassLoader()), "classes/META-INF/persistence.xml");
        archive.addAllDependencies();
        logger.infof("Content of war file %s:\n",archive.toString(true));
        return archive;
    }

    private static DatasourcesFraction datasource(Swarm swarm) {
        logger.infof("Using database.url: %s",swarm.stageConfig().resolve("database.url").getValue());
        logger.infof("Using database.username: %s",swarm.stageConfig().resolve("database.username").getValue());
        return new DatasourcesFraction()
                .jdbcDriver("net.sourceforge.jtds",(d)->{
                    d.driverClassName("net.sourceforge.jtds.jdbc.Driver");
                    d.xaDatasourceClass("net.sourceforge.jtds.jdbcx.JtdsDataSource");
                    d.driverModuleName("net.sourceforge.jtds");
                })
                .dataSource("demoDS",(ds)->{
                    ds.driverName("net.sourceforge.jtds");
                    ds.connectionUrl(swarm.stageConfig().resolve("database.url").getValue());
                    ds.userName(swarm.stageConfig().resolve("database.username").getValue());
                    ds.password(swarm.stageConfig().resolve("database.password").getValue());
                });
    }

    private static LoggingFraction logging() {

        final String logFile = System.getProperty("user.dir") + File.separator+
                "target"+File.separator+
                "cliente.log";
        logger.infof("Writing logs to %s",logFile);

        LoggingFraction loggingFraction = new LoggingFraction()
                .periodicSizeRotatingFileHandler("demo-log",(h)->{
                    h.level(Level.INFO)
                            .append(true)
                            .suffix(".yyyy-MM-dd")
                            .rotateSize("30m")
                            .enabled(true)
                            .encoding("UTF-8")
                            .maxBackupIndex(2);
                    Map<String,String> fileSpec = new HashMap<>();
                    fileSpec.put("path", logFile);
                    h.file(fileSpec);
                }).logger("org.demo",(l)->{
                    l.level(Level.INFO)
                            .handler("demo-log");
                });
        return loggingFraction;
    }

}
