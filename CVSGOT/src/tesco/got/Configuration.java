/*
 * 작성된 날짜: 2005. 6. 23. 오후 6:52:21
 * 
 * 김한영(duloveme@hotmail.com)
 * 
 */
package tesco.got;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author 김한영 (duloveme@hotmail.com)
 *
 * 2005. 6. 23. 오후 6:52:21
 * Configuration.java
 * 
 */

public class Configuration {
    private int port = 10030;
    private int poolCount = 15;
    private int version = 1;
    private String adminId = "admin_id";
    private String adminPwd = "admin_pwd";
    private Properties prop = null;
    private String logFolder = "/home/cvsapp/pda/log";
    private static Configuration thisObj = null;
    private String filename = "tesco.server.properties";
    private String updateServer = "10.10.38.34";
    /**
     * 
     */
    private Configuration() throws Exception{
        this.prop = new Properties();
        loadProperties();
    }
    
    public static Configuration getInstance() throws Exception{
        if(thisObj == null){
            thisObj = new Configuration();
        }
        return thisObj;
    }
    
    /**
     * 설정파일 로드
     * @param filename 설정파일명
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void loadProperties() throws FileNotFoundException,IOException,Exception{
                
        File file = new File(filename);
        if (file.exists()) {
            
            FileInputStream fis = new FileInputStream(file);
            prop.load(fis);
            
            this.logFolder		= prop.getProperty("log_folder",this.logFolder);
            this.port			= Integer.parseInt(prop.getProperty("port",String.valueOf(this.port)));
            this.poolCount 		= Integer.parseInt(prop.getProperty("poolCount",String.valueOf(this.poolCount)));            
            this.adminId  		= prop.getProperty("admin_id",this.adminId);
            this.adminPwd 		= prop.getProperty("admin_pwd",this.adminPwd);
            this.version 		= Integer.parseInt(prop.getProperty("version",String.valueOf(this.version)));
            this.updateServer	= prop.getProperty("upgrade_server",this.updateServer);

            fis.close();
            
        } else {
            
            saveConfiguration();
        }
    }
    
    public void saveConfiguration() throws FileNotFoundException,IOException,Exception{
        File file = new File(this.filename);
        
        FileOutputStream fos = new FileOutputStream(file);
        
        prop.setProperty("log_folder",this.logFolder);
        prop.setProperty("port",String.valueOf(this.port));
        prop.setProperty("poolCount",String.valueOf(this.poolCount));
        prop.setProperty("admin_id",this.adminId);
        prop.setProperty("admin_pwd",this.adminPwd);
        prop.setProperty("version",String.valueOf(this.version));
        prop.setProperty("upgrade_server",this.updateServer);
        
        prop.store(fos,"Tesco Server Setting");
        
        fos.close();
    }
    /**
     * @return adminId을 리턴합니다.
     */
    public String getAdminId() {
        return adminId;
    }
    /**
     * @param adminId 설정하려는 adminId.
     */
    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }
    /**
     * @return adminPwd을 리턴합니다.
     */
    public String getAdminPwd() {
        return adminPwd;
    }
    /**
     * @param adminPwd 설정하려는 adminPwd.
     */
    public void setAdminPwd(String adminPwd) {
        this.adminPwd = adminPwd;
    }
    /**
     * @return logFolder을 리턴합니다.
     */
    public String getLogFolder() {
        return logFolder;
    }
    /**
     * @param logFolder 설정하려는 logFolder.
     */
    public void setLogFolder(String logFolder) {
        this.logFolder = logFolder;
    }
    /**
     * @return poolCount을 리턴합니다.
     */
    public int getPoolCount() {
        return poolCount;
    }
    /**
     * @param poolCount 설정하려는 poolCount.
     */
    public void setPoolCount(int poolCount) {
        this.poolCount = poolCount;
    }
    /**
     * @return port을 리턴합니다.
     */
    public int getPort() {
        return port;
    }
    /**
     * @param port 설정하려는 port.
     */
    public void setPort(int port) {
        this.port = port;
    }
    /**
     * @return 업데이트 서버 IP을 리턴합니다.
     */
    public String getUpdateServer() {
        return updateServer;
    }    
    /*
     * @param ip 설정하려는 업데이트 서버
     */
    public void setUpdateServer(String ip) {
        this.updateServer = ip;
    }
    /**
     * @return prop을 리턴합니다.
     */
    public Properties getProp() {
        return prop;
    }
    /**
     * @param prop 설정하려는 prop.
     */
    public void setProp(Properties prop) {
        this.prop = prop;
    }
    /**
     * @return version을 리턴합니다.
     */
    public int getVersion() {
        return version;
    }
    /**
     * @param version 설정하려는 version.
     */
    public void setVersion(int version) {
        this.version = version;
    }
}
