/*****************************************************
 * PROGRAM ID    : Server
 * PROGRAM NAME	 : 
 * CREATED BY	 : 이태성
 * CREATION DATE : 2011.11.14
 * 김한영(duloveme@hotmail.com) 소스를 수정 하여 만듬
 *****************************************************
 *****************************************************
 *  변경일자    /  변경자  / 변경사유 

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
 * 커넥션 풀링을 이용하여 클라이언트 연결을 처리합니다.
 */

public class Server {
    private static Logger logger = null;    
    private boolean flag = true;
    private ServerSocket svrSock = null;
    private static Configuration config = null;
    private KillSession killer = null;
    
        /**
     * 설정 파일을 읽어옵니다.
     * @param filename 설정파일 명
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void loadProperties(String filename) throws FileNotFoundException,IOException{
        
    }
    
    /**
     * 생성자
     * @param port 연결 대기 Port
     * @param logger 로그인터페이스를 상속받은 클래스 또는 가 하위 클래스
     * @throws IOException
     * @throws SQLException
     */
    public Server(Logger logger,Configuration config) throws Exception{
    	Server.config =  config;
        
        loadProperties("tesco.server.properties");
                
//        // 데이터 베이스 연결 발생
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

            // 일정시간 경과한 Session 삭제
            killer = new KillSession(true);
            killer.start();
            
            while(flag){                
                Socket sock = this.svrSock.accept();                 
                ClientThread ct = new ClientThread(sock, Server.logger, this.config);
                ct.start();
            }
        } catch (Exception e) {
            if(Server.logger != null){
                Server.logger.writeEntry("start()함수"+e);
            }
        } finally {
            try{
                ConnPool.destroyConnPool();
            } catch (Exception e) {}
        }             
    }
    
    /**
     * 서버를 중지합니다.
     */
    public void stop(){
        this.flag = false;
        killer.setStop();
    }
    /**
     * @return version을 리턴합니다.
     */
    public static int getVersion() {
        return Server.config.getVersion();
    }
    
    public static void VersionUp(Object[] params) throws GotException,Exception{
        String admin_id = params[3].toString();
        String admin_pwd = params[4].toString();
        
        if((!admin_id.equals(Server.config.getAdminId()))||(!admin_pwd.equals(Server.config.getAdminPwd()))){
            throw new GotException("관리자 아이디/비밀번호가 올바르지 않습니다");
        }
        
        config.setVersion(config.getVersion()+1);
                
       try{
       		config.saveConfiguration();
       } catch (Exception e){
           throw new GotException("버전업 실패");
       }
       
    }
    
    public static Logger getLogger()
    {
    	return Server.logger;
    }
}
