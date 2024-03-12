/*****************************************************
 * PROGRAM ID    : AceeptThread
 * PROGRAM NAME	 : 
 * CREATED BY	 : ���¼� (HBO���� �̿���)
 * CREATION DATE : 2011.11.14
 *****************************************************
 *****************************************************
 *  ��������    /  ������  / ������� 

 ******************************************************/
package tesco.got;
 
import java.net.ServerSocket;
import java.net.Socket;
import awoo.util.Logger;

/**
 * @author ���ѿ� (duloveme@hotmail.com)
 *
 * 2005. 6. 2. ���� 11:08:41
 * AcceptThread.java
 * <br>
 * Ŭ���̾�Ʈ ������ ó���մϴ�.
 */

public class AcceptThread extends Thread {
    private boolean flag = true;
    private ServerSocket sock = null;
    private Logger logger = null;
    private Configuration config = null;
    /**
     * 
     * @param sock ���� ����
     * @param logger Logger �������̽��� ��ӹ��� Ŭ���� �Ǵ� �� ���� Ŭ����
     */
    public AcceptThread(ServerSocket sock,Logger logger, Configuration config){
        this.sock = sock;
        this.logger = logger;
        this.config = config;
    }
    
    /* (��Javadoc)
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
            this.logger.writeEntry("AcceptThread.Start()�Լ�"+e);
        }
    }
    
    /**
     * �����带 �����մϴ�.
     */
    public void setStop(){
        this.flag = false;
    }

}
