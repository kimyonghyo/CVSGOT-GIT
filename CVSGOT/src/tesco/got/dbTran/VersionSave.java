/*****************************************************
 * PROGRAM ID    : VersionSave
 * PROGRAM NAME  : Login 정보 저장
 * CREATED BY    : 이태성
 * CREATION DATE : 2012/02/16
 ***************************************************** 
 * 2013/02/02 Version Update Process 개선
 *****************************************************/ 
package tesco.got.dbTran;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import awoo.dbUtil.ConnPool;
import awoo.dbUtil.OraConnFactory;

public class VersionSave {

	private int store_code = 0;      
	private String version = "";
	private String ip = "";
	private String dir = "";
	private String user_id = "";
	private String use_yn = "";
	private String os_version = "";
  
    public VersionSave(String store_code, String version, String ip, String dir, String user_id, String use_yn, String os_version) {
    	this.store_code = Integer.parseInt(store_code);
    	this.version = version;
    	this.ip = ip;
    	this.dir = dir;
    	this.user_id = user_id;
    	this.use_yn = use_yn;
    	this.os_version = os_version;
    }    
	/**
	 * 
	 * @return
	 * @throws GotException
	 * @throws Exception
	 */
	public void executeQuery() throws GotException,Exception {
		StringBuffer sql = null;
				
		Connection conn = null;
        ConnPool cp = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        int affectedRow = 0;
                  
		try {
			cp = ConnPool.getInstance();
			conn = cp.getConnection();
			
			sql = new StringBuffer();
			sql.append("SELECT COUNT(*) CNT  ");
			sql.append("  FROM PDA_VERSION   ");
			sql.append(" WHERE STORE_NO = ?  ");
			sql.append("   AND PDA_IP = ?    ");
			
			pstmt = conn.prepareStatement(sql.toString());
        	pstmt.setInt(1, this.store_code);
        	pstmt.setString(2, this.ip);

        	rs = pstmt.executeQuery(); 		

        	if(rs.next() && (rs.getInt(1)==0)){        		        	
        		
        		rs.close();
        		pstmt.close();
        		
        		sql = new StringBuffer();
        		sql.append(" INSERT INTO PDA_VERSION (STORE_NO, PDA_IP, PDA_VER, DIR, USER_ID, UPDATE_DATE, OS_VER) VALUES (?, ?, ?, ?, ?, SYSDATE, ?)  ");
        		
        		pstmt = conn.prepareStatement(sql.toString());
        		pstmt.setInt(1, this.store_code);
        		pstmt.setString(2, this.ip);
        		pstmt.setString(3, this.version);
        		pstmt.setString(4, this.dir);
        		pstmt.setString(5, this.user_id);
        		pstmt.setString(6, this.os_version);
        		
        		affectedRow = pstmt.executeUpdate();   
        		
        	}else{
        		
        		rs.close();
        		pstmt.close();        		
        		
        		sql = new StringBuffer();
        		sql.append(" UPDATE PDA_VERSION SET PDA_VER = ?, UPDATE_DATE = SYSDATE, USER_ID = ?, USE_YN = ?, LOGBOOK_YN = 'N', OS_VER = ? ");
        		sql.append("  WHERE STORE_NO = ?   ");
        		sql.append("    AND PDA_IP   = ?   ");
            	
        		pstmt = conn.prepareStatement(sql.toString());            	
        		pstmt.setString(1, this.version);
        		pstmt.setString(2, this.user_id);
        		pstmt.setString(3, this.use_yn);        		
        		pstmt.setString(4, this.os_version);
        		pstmt.setInt(5, this.store_code);
            	pstmt.setString(6, this.ip);
            	
            	affectedRow = pstmt.executeUpdate();	            	
        	}
        	
        	rs.close();
        	pstmt.close();
        	
            if (affectedRow <= 0){
                throw new GotException("PDA Version 정보 저장 중 오류가 발생했습니다");
            }            	
		}		
		catch(SQLException se) {
            try {
            	conn.rollback();
            	conn.close();
            } catch (Exception e) {}                  
            conn = OraConnFactory.getInstance().getConnection();
            throw se;
              
        } catch(Exception e) {
            throw e;		      
		} finally {
			try{
		        if (rs != null)
		            rs.close();
		    } catch(Exception e){}
			try{
		        if (pstmt != null)
		            pstmt.close();
		    } catch(Exception e){}
		    try{
		        cp.releaseConnection(conn);
		    } catch(Exception e){}
		}
	}
}
	
	

