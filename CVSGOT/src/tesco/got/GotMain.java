/*****************************************************
 * PROGRAM ID    : GotMain
 * PROGRAM NAME	 : 
 * CREATED BY	 : 이태성 (HBO 소스를 참조 - 김한영 (duloveme@hotmail.com))
 * CREATION DATE : 2012.01.13
 *****************************************************
 *****************************************************
 *  변경일자    /  변경자  / 변경사유 

 ******************************************************/
package tesco.got;

import java.io.FileInputStream;
import java.util.Properties;

import awoo.dbUtil.ConnPool;
import awoo.dbUtil.OraConnFactory;
import awoo.util.FileLogger;
import awoo.util.Logger;


public class GotMain { 
    //private static String log_folder = "";
    //private static Properties prop = null;
    private static Configuration config = null;
    private static Logger logger = null;
    public static void main(String[] args) throws Exception{
        //logger = new ConsoleLogger();
        config = Configuration.getInstance();
        logger = new FileLogger(config.getLogFolder());

        try{
        	// 데이터베이스 풀링 초기화
        	initDbPooling(logger);
            // 서버 생성
            Server svr = new Server(logger,config);
            logger.writeEntry("heapsize : " + String.valueOf(Runtime.getRuntime().totalMemory()) );
            logger.writeEntry("maxmemory : " + String.valueOf(Runtime.getRuntime().maxMemory()) );
            logger.writeEntry("freememory : " + String.valueOf(Runtime.getRuntime().freeMemory()) );
            logger.writeEntry("Start Server");
            svr.start();
            logger.writeEntry("Stop Server");
        } catch (Exception e){
        	System.out.println(e);
            logger.writeEntry("Gotmain()함수"+e);
        } finally {
            config.saveConfiguration();
        }
    }
    
    /*
     * Database Pooling initailize
     */
    private static void initDbPooling(Logger logger) throws Exception
    {
    	FileInputStream fis = new FileInputStream("tesco.db.properties");
    	try
    	{
    		Properties dbProp = new Properties();
        	dbProp.load(fis);

        	String sid  = dbProp.getProperty("sid");

        	if ((sid == "")||(sid == null)) {
				sid = "CSMPRD1";
			}

        	//System.out.println("step1 :");
        	OraConnFactory.Initialize(
        			dbProp.getProperty("url"),
        			sid,
        			dbProp.getProperty("user"),
        			dbProp.getProperty("password"),
        			Integer.parseInt(dbProp.getProperty("port")),
        			Integer.parseInt(dbProp.getProperty("maxConn")));
        	//System.out.println("step2 :");
        	ConnPool.Initialize(OraConnFactory.getInstance());
        	//System.out.println("step3 :");     
        	logger.writeEntry("DB Connection info");
        	logger.writeEntry("url : " + dbProp.getProperty("url"));
        	logger.writeEntry("sid : " + dbProp.getProperty("sid"));
        	
        	System.out.println("success");
    	}
    	finally
    	{
    		try
    		{
    			fis.close();
    		}catch(Exception e){}
    	}
    }
    
}
