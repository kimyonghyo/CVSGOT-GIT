/*****************************************************
 * PROGRAM ID    : KillSession
 * PROGRAM NAME	 : 보관주기 지난 세션은 제거 
 * CREATED BY	 : jskoo
 * CREATION DATE : 2013.11.29
 *****************************************************
 *****************************************************
 *  변경일자    /  변경자  / 변경사유 
 ******************************************************/
package tesco.got;

import java.util.Hashtable;

public class KillSession extends Thread {
	int expireTimeOut = 180; // 60분설정 -> 2015-03-16 수정. 이종욱. 기존 60분에서 3시간으로 세션타임 증가.
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
	        		Thread.sleep(60000 * 5); //5 분 간격
				}
        	}            
        } catch (Exception e) {
            
        }
    }

    public void setStop(){
    	this.bRunFlag = false;
    }
}
