/*****************************************************
 * PROGRAM ID    : Server
 * PROGRAM NAME	 : 
 * CREATED BY	 : ���¼�
 * CREATION DATE : 2011.11.14
 * ���ѿ�(duloveme@hotmail.com) �ҽ��� ���� �Ͽ� ����
 *****************************************************
 *****************************************************
 *  ��������    /  ������  / ������� 

 ******************************************************/
package tesco.got;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;

import tesco.got.dbTran.GotException;
import awoo.dbUtil.ConnPool;
import awoo.util.Logger;

/**
 * Ŀ�ؼ� Ǯ���� �̿��Ͽ� Ŭ���̾�Ʈ ������ ó���մϴ�.
 */

public class Server {
    private static Logger logger = null;    
    private boolean flag = true;
    private ServerSocket svrSock = null;
    private static Configuration config = null;
    private KillSession killer = null;
    
        /**
     * ���� ������ �о�ɴϴ�.
     * @param filename �������� ��
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void loadProperties(String filename) throws FileNotFoundException,IOException{
        
    }
    
    /**
     * ������
     * @param port ���� ��� Port
     * @param logger �α��������̽��� ��ӹ��� Ŭ���� �Ǵ� �� ���� Ŭ����
     * @throws IOException
     * @throws SQLException
     */
    public Server(Logger logger,Configuration config) throws Exception{
    	Server.config =  config;
        
        loadProperties("tesco.server.properties");
                
//        // ������ ���̽� ���� �߻�
//        ConnPool connPool = ConnPool.getInstance();
        
        Server.logger  = logger;
        
    }
    
    public void start() throws Exception {    	
    	int poolCount = config.getPoolCount();
    	
        try{
            this.svrSock = new ServerSocket(config.getPort());

            for(int i = 0; i < poolCount; i++){
                AcceptThread at = new AcceptThread(this.svrSock,Server.logger, this.config);
                at.start();
            }

            // �����ð� ����� Session ����
            killer = new KillSession(true);
            killer.start();
            
            while(flag){                
                Socket sock = this.svrSock.accept();                 
                ClientThread ct = new ClientThread(sock, Server.logger, this.config);
                ct.start();
            }
        } catch (Exception e) {
            if(Server.logger != null){
                Server.logger.writeEntry("start()�Լ�"+e);
            }
        } finally {
            try{
                ConnPool.destroyConnPool();
            } catch (Exception e) {}
        }             
    }
    
    /**
     * ������ �����մϴ�.
     */
    public void stop(){
        this.flag = false;
        killer.setStop();
    }
    /**
     * @return version�� �����մϴ�.
     */
    public static int getVersion() {
        return Server.config.getVersion();
    }
    
    public static void VersionUp(Object[] params) throws GotException,Exception{
        String admin_id = params[3].toString();
        String admin_pwd = params[4].toString();
        
        if((!admin_id.equals(Server.config.getAdminId()))||(!admin_pwd.equals(Server.config.getAdminPwd()))){
            throw new GotException("������ ���̵�/��й�ȣ�� �ùٸ��� �ʽ��ϴ�");
        }
        
        config.setVersion(config.getVersion()+1);
                
       try{
       		config.saveConfiguration();
       } catch (Exception e){
           throw new GotException("������ ����");
       }
       
    }
    
    public static Logger getLogger()
    {
    	return Server.logger;
    }
}
