/*****************************************************
 * PROGRAM ID    : LgbSave
 * PROGRAM NAME  : 로그북 저장
 * CREATED BY    : 이태성
 * CREATION DATE : 2012/02/08
 *****************************************************/ 
package tesco.got.dbTran;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import tesco.got.Common;

import awoo.dbUtil.ConnPool;
import awoo.dbUtil.OraConnFactory;
import awoo.util.StringUtil;

public class LgbSave {

	private String store_code = "";      
    private String date = "";
	private Object[] items = null;
	private String user_id = "";
       
    public LgbSave(String store_code, String date, Object[] items, String user_id) {
    	this.store_code = store_code;        
    	this.date = date;
    	this.items = items;
    	this.user_id = user_id;    	       
    }    
	/**	 
	 * @return
	 * @throws GotException
	 * @throws Exception
	 */
	public void executeQuery() throws GotException,Exception {
	    Connection conn = null;
        ConnPool cp = null;
        PreparedStatement pstmt = null;
        PreparedStatement pstmt2 = null;
        
        ResultSet rs = null;	     
        
		try {
			cp = ConnPool.getInstance();
			conn = cp.getConnection();
			
			StringBuffer sql = null;			
			StringBuffer sql2 = null;
			StringBuffer sql3 = null;
			StringBuffer sql4 = null;
			
			String check_id = "";
			String log_id = "";
			String checked= "";
            String act_id= "";
            String action_id	=	"";           
    		
    		for(int i = 4; i < this.items.length; i++){

	               Object[] tmp = StringUtil.split(this.items[i].toString(),Common.GS);
	
	                log_id 		= tmp[0].toString();	                
	                checked		= tmp[1].toString();
	                act_id			= tmp[2].toString();
	                
	                sql=null;
	                sql = new StringBuffer();
	                sql.append("   SELECT to_char(nvl(MAX(CHK_ID),0)+1) AS CHECK_ID   ");                       
	                sql.append("   FROM E_LOG_BOOK   ");            

	                pstmt = conn.prepareStatement(sql.toString());			
	        		rs = pstmt.executeQuery();
	        			
	        		if(rs.next()){
	        			check_id = rs.getString("CHECK_ID");
	        		}
	        		rs.close();
	        		pstmt.close();
	
	                sql2 = new StringBuffer();
	        		sql2.append("   INSERT INTO E_LOG_BOOK(CHK_ID, STORE, LOG_DATE, LOG_ID, EMP_ID, CHECKED, CHK_DATE, SM_YN)   ");    		
	        		sql2.append("   VALUES (   ");
	        		sql2.append("       TO_NUMBER(?),   ");										//	chk_id
	        		sql2.append("   		TO_NUMBER(" + this.store_code + "),   ");
	        		sql2.append("   		?,   ");														//	date
	        		sql2.append("   		TO_NUMBER(?),   ");									// log_id
	        		sql2.append("   		" + this.user_id + " ,   ");
	        		sql2.append("   		TO_NUMBER(?),   ");									//	checked
	        		sql2.append("   		SYSDATE,	   ");
	        		sql2.append("   		NULL   ");
	        		sql2.append("   )   ");
	        		    
	        		pstmt = conn.prepareStatement(sql2.toString()); 
	        		
	    			pstmt.setString(1,check_id);
	    			pstmt.setString(2,this.date);
	    			pstmt.setString(3,log_id);
	    			pstmt.setString(4,checked);			
  			
	    			if(pstmt.executeUpdate() > 0){
	    				
	    				pstmt.close();
	    				
	    				if(checked.equalsIgnoreCase("2")){
	    					sql3 = new StringBuffer();
	    					sql3.append("   SELECT to_char(nvl(MAX(ACT_ID),0)+1)  AS ACT_ID ");	    					
	    					sql3.append("   FROM E_LOG_ACTION   ");
	    						
	        				pstmt = conn.prepareStatement(sql3.toString());
	        				rs = pstmt.executeQuery();
	        				
	        				if(rs.next()){
	        					action_id	=	rs.getString("ACT_ID");
	        				}
	        				
	        				rs.close();
	        				pstmt.close();
	        				
	        				sql4 = new StringBuffer();
	        				sql4.append("   INSERT INTO E_LOG_ACTION   ");	        				
	        				sql4.append("       (ACT_ID, CHK_ID, ACTION_ID, SHELF_ID, SM_ID, SHELF_ACTION, SM_ACTION, SHELF_DATE, SM_DATE)   ");
	        				sql4.append("   VALUES   ");
	        				sql4.append("       (   ");
	        				sql4.append("           TO_NUMBER(?),   ");
	        				sql4.append("           TO_NUMBER(?),   ");
	        				sql4.append("           TO_NUMBER(?),   ");
	        				sql4.append("           " + this.user_id + ",   ");
	        				sql4.append("           NULL,   ");
	        				sql4.append("           NULL,   ");
	        				sql4.append("           NULL,   ");
	        				sql4.append("           SYSDATE,   ");
	        				sql4.append("           NULL   ");
	        				sql4.append("       )   ");
	        			
	        				pstmt2 = conn.prepareStatement(sql4.toString());
	        				
	        				pstmt2.setString(1,action_id);
	            			pstmt2.setString(2,check_id);
	            			pstmt2.setString(3,act_id);
	            			
	            			if(pstmt2.executeUpdate() <= 0)
	            			{
	            				throw new GotException("이미 처리된 분류 입니다.");	        							
	            			}
	            			pstmt2.close();
	    				}
    					
    				}else{
    					throw new GotException("처리실패. 재시도 하세요.");    					
    				}            
            }
              
		    pstmt.close();
			            
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
                    if (pstmt2 != null)
                        pstmt2.close();
                } catch(Exception e){}
               try{
                    cp.releaseConnection(conn);
                } catch(Exception e){}
            }
        }
    }