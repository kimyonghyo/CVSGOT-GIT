/*****************************************************
 * PROGRAM ID    : KillSession
 * PROGRAM NAME	 : �����ֱ� ���� ������ ���� 
 * CREATED BY	 : jskoo
 * CREATION DATE : 2013.11.29
 *****************************************************
 *****************************************************
 *  ��������    /  ������  / ������� 
 ******************************************************/
package tesco.got;

import java.util.Hashtable;

public class KillSession extends Thread {
	int expireTimeOut = 180; // 60�м��� -> 2015-03-16 ����. ������. ���� 60�п��� 3�ð����� ����Ÿ�� ����.
	boolean bRunFlag  = true;			
	
    public KillSession(boolean runFlag){
    	this.bRunFlag = runFlag;
    }
    
    public void run(){
        try{
        	while(this.bRunFlag) {
				SessionManager sm = SessionManager.getInstatnce();
				if(sm != null) {
					sm.killTimeoutSession(expireTimeOut); 
	        		Thread.sleep(60000 * 5); //5 �� ����
				}
        	}            
        } catch (Exception e) {
            
        }
    }

    public void setStop(){
    	this.bRunFlag = false;
    }
}
