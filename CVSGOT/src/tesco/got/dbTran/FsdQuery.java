/*****************************************************
 * PROGRAM ID    : FsdQuery
 * PROGRAM NAME  : 신선재고조사 날짜 조회
 * CREATION DATE : 2013
 *****************************************************/ 
package tesco.got.dbTran;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import tesco.got.Common;

import awoo.dbUtil.ConnPool;
import awoo.dbUtil.OraConnFactory;


public class FsdQuery {
	private String _s_store_code = "";
	private int    _i_store_code = 0;
	
	/**
     * @param store_code
     * @param survey_date
     * @param item_code
     */
    public FsdQuery(String store_code) {
    	this._s_store_code = store_code;
    	this._i_store_code = Integer.parseInt(store_code);     	
    }
    
    public String executeQuery() throws GotException,Exception{
    	StringBuffer sql = null;    	
    	
        Connection conn = null;
        ConnPool cp = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        String date	= "";
        
        try{
            cp = ConnPool.getInstance();
            conn = cp.getConnection();
            
            sql = new StringBuffer();
            sql.append("SELECT TO_CHAR(PARM_DATE, 'YYYYMMDD') AS PARM_DATE");
            sql.append("  FROM PARAMETER A "				);
            sql.append(" WHERE A.STORE_NO  = ? "			);
            sql.append("   AND A.PARM_NAME = 'FSH_CNT_STAT'");
            sql.append("   AND ROWNUM      = 1   ");
            
			pstmt = conn.prepareStatement(sql.toString());
			pstmt.setInt(1, this._i_store_code);
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				date = rs.getString("PARM_DATE");
			}else{
				throw new GotException("신선재고조사일 조회 실패!");
			}

			rs.close();
			pstmt.close();
			
            StringBuffer tmp = new StringBuffer();
            tmp.append(date);
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