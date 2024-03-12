/*****************************************************
 * PROGRAM ID    : AceeptThread
 * PROGRAM NAME	 : 
 * CREATED BY	 : 이태성 (HBO데몬 이용함)
 * CREATION DATE : 2011.11.14
 *****************************************************
 *****************************************************
 *  변경일자    /  변경자  / 변경사유 

 ******************************************************/
package tesco.got;

import java.net.ServerSocket;
import java.net.Socket;
import awoo.util.Logger;

/**
 * @author 김한영 (duloveme@hotmail.com)
 *
 * 2005. 6. 2. 오전 11:08:41
 * AcceptThread.java
 * <br>
 * 클라이언트 연결을 처리합니다.
 */

public class AcceptThread extends Thread {
    private boolean flag = true;
    private ServerSocket sock = null;
    private Logger logger = null;
    private Configuration config = null;
    /**
     * 
     * @param sock 서버 소켓
     * @param logger Logger 인터페이스를 상속받은 클래스 또는 그 하위 클래스
     */
    public AcceptThread(ServerSocket sock,Logger logger, Configuration config){
        this.sock = sock;
        this.logger = logger;
        this.config = config;
    }
    
    /* (비Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run(){
        try{
            while(flag){
                Socket client = this.sock.accept();              
                ClientThread ct = new ClientThread(client,this.logger,this.config);
                ct.start();
            }
        }catch (Exception e) {
            this.logger.writeEntry("AcceptThread.Start()함수"+e);
        }
    }
    
    /**
     * 쓰레드를 중지합니다.
     */
    public void setStop(){
        this.flag = false;
    }

}
