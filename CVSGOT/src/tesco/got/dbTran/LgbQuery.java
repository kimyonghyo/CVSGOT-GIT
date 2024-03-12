/*****************************************************
 * PROGRAM ID    : LgbQuery
 * PROGRAM NAME  : 로그북 항목설명 조회
 * CREATED BY    : 이태성
 * CREATION DATE : 2012/02/07
 *****************************************************/ 
package tesco.got.dbTran;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import tesco.got.Common;

import awoo.dbUtil.ConnPool;
import awoo.dbUtil.OraConnFactory;


public class LgbQuery {
	private String log_id = "";
	
	/**
     * @param store_code
     * @param log_id
     */
    public LgbQuery(String log_id) {
    	this.log_id	=	log_id;
    }
    
    public String executeQuery() throws GotException,Exception{
    	StringBuffer sql = null;
        
        Connection conn = null;
        ConnPool cp = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try{
            cp = ConnPool.getInstance();
            conn = cp.getConnection();
            
            String desc = "";

            sql = new StringBuffer();
            sql.append("   SELECT GUIDE_DESC   ");
            sql.append("   FROM E_LOG_GUIDE   ");
            sql.append("   WHERE LOG_ID = TO_NUMBER(?)   ");
            sql.append("       AND GUIDE_GB = 1    ");
            
			pstmt = conn.prepareStatement(sql.toString());		
			pstmt.setString(1, this.log_id);
			rs = pstmt.executeQuery();
			
			 if (!rs.next()){
                 throw new GotException("상세 항목이 없습니다.");
             }

			desc = rs.getString("GUIDE_DESC");

			rs.close();                
            pstmt.close();                     

		    
			StringBuffer tmp = new StringBuffer();
            tmp.append(desc);
            tmp.append(Common.FS);

            return tmp.toString();                
        }
        catch(SQLException se){
        	try
        	{
        		conn.close();
        	}
        	catch (Exception e) {}        	
        	conn = OraConnFactory.getInstance().getConnection();
        	throw se;
        }
        catch(Exception e){
        	throw e;
        }
        finally{
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