/*****************************************************
 * PROGRAM ID    : RtiSave
 * PROGRAM NAME  : 반품등록 저장부분
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
import awoo.util.StringUtil;

public class RtiSave {
	private String _s_store_code = "";
	private int    _i_store_code = 0;
	
	private Object[] _items = null;
	private String _user_id = "";
  
    public RtiSave(String store_code, Object[] items, String user_id) {
    	this._s_store_code = store_code;
    	this._i_store_code = Integer.parseInt(store_code);     
    	this._items	       = items;
    	this._user_id	   = user_id;
    }    
	/** 
	 * @return
	 * @throws GotException
	 * @throws Exception
	 */
	public int executeQuery() throws GotException,Exception {
		StringBuffer sql1 = null;		
		Connection conn = null;
        ConnPool cp = null;
        ResultSet rs = null;        
        PreparedStatement pstmt1 = null;        

		try {
			cp = ConnPool.getInstance();
			conn = cp.getConnection();

			String item_code   = "";
			String qty		   = "";
			String price	   = "";			
            String resaon_code = "";
			String return_date = "";
            
            int rejCount = 0;    
                       
            sql1 = new StringBuffer();		
            sql1.append("INSERT INTO FSM_RETURN_REQUEST");
            sql1.append("("								);
            sql1.append("		REQUEST_NO,   "			);
            sql1.append("		STORE_NO,     "			);
            sql1.append("		REQUEST_DATE, "			);
            sql1.append("		ITEM_NO,      "			);
            sql1.append("		ITEM_NAME,    "			);
            sql1.append("		STOCK_CATE,   "			);
            sql1.append("		ON_HAND_QTY,  "			);
            sql1.append("		RETURN_DATE,  "			);
            sql1.append("		RETURN_QTY,   "			);
            sql1.append("		SUPPLIER_NO,  "			);
            sql1.append("		SUPPLIER_NAME,"			);
            sql1.append("		DC_NO,        "			);
            sql1.append("		DC_NAME,      "			);
            sql1.append("		RETURN_REASON,"			); 
            sql1.append("		STATUS,       "			);
            sql1.append("		REQUEST_TIME, "			);
            sql1.append("		CREATE_RESNO  "			);  
            sql1.append(")");
            sql1.append("SELECT NVL((SELECT MAX(REQUEST_NO) REQUEST_NO FROM FSM_RETURN_REQUEST), 0) + 1, "); // request_no
            sql1.append("     	?, "								); // store_no
            sql1.append("     	TO_CHAR(SYSDATE,'YYYYMMDD'), "		);
            sql1.append("     	A.ITEM_NO, "						);
            sql1.append("     	A.SHORT_DESCR, "					);
            sql1.append("     	DECODE(A.STOCK_CATE,1,'D',2,'C',3,'W',NULL,'D') STOCK_CATE, ");
            sql1.append("     	NVL((SELECT ON_HAND_QTY "			);
            sql1.append("     		   FROM STOCK_ON_HAND "			);
            sql1.append("     		  WHERE LOC_NO   = 20 "			);
            sql1.append("     		    AND STORE_NO = A.STORE_NO "	);
            sql1.append("     		    AND ITEM_NO  = A.ITEM_NO), 0),");
            sql1.append("     	?, "								); // return_date
            sql1.append("       TO_NUMBER(replace(?, ',', '')), "	); // return Qty
            sql1.append("     	A.SUPPLIER_NO, "					); // supplier_no
            sql1.append("     	(select VENDOR_KOR_NM from SUPS WHERE SUPPLIER_NO = A.SUPPLIER_NO), "); // supplier_nm
            sql1.append("     	A.SOURCE_WH, "						); // dc_no
            sql1.append("     	(SELECT STORE_NAME FROM STORES WHERE STORE_NO = A.SOURCE_WH), "); // dc_nm
            sql1.append("     	?, "								); // resaon_code           
            sql1.append("     	0, "								); // status
            sql1.append("     	TO_CHAR(SYSDATE,'HH24MISS'), "		); // request time
            sql1.append("     	? "									); // user_id
            sql1.append("  FROM PLU A "								);
            sql1.append(" WHERE A.ITEM_NO  = ?"						);
            sql1.append("   AND A.STORE_NO = ?"						);
   
        	for(int i = 3; i < this._items.length; i++){
        		Object[] tmp = StringUtil.split(this._items[i].toString(), Common.GS);

			    item_code 	= tmp[0].toString();
			    qty			= tmp[1].toString();
			    resaon_code	= tmp[2].toString();			    
			    return_date	= tmp[3].toString();			    
			    
			    if(CheckRtiBlock(conn, item_code)) {
			    	rejCount++;
			    	continue;
			    }
			    
			    pstmt1 = conn.prepareStatement(sql1.toString());
			    
			    pstmt1.setInt(1, this._i_store_code);			    
			    pstmt1.setString(2, return_date);
			    pstmt1.setString(3, qty);
			    pstmt1.setString(4, resaon_code);		    
			    pstmt1.setString(5, _user_id);			    
			    pstmt1.setString(6, item_code);
			    pstmt1.setInt(7, this._i_store_code);
			    try {
				    if(pstmt1.executeUpdate() <=0){
				    	rejCount++;
				    }	 
			    }catch (SQLException se) {
			    	rejCount++;
			    }
			}        	
        	return rejCount;			
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
			     if (pstmt1 != null)
			         pstmt1.close();
			    } catch(Exception e){}
		   try{
		        cp.releaseConnection(conn);
		    } catch(Exception e){}
		}
	}
	
	public boolean CheckRtiBlock(Connection conn, String item_no) throws GotException,Exception {
		StringBuffer sql = null;		
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;    
	    String seq = "1";
	    
	    sql = new StringBuffer();
	    
	    try {
			sql.append("SELECT A.STOCK_CATE, B.STATUS"		); 
			sql.append("  FROM PLU A, FSM_RETURN_REQUEST B" );
			sql.append(" WHERE A.STORE_NO = B.STORE_NO"		);
			sql.append("   AND A.ITEM_NO  = B.ITEM_NO"		);
			sql.append("   AND A.ITEM_NO  = ? "				);
			sql.append("   AND A.STORE_NO = ? "				);
			sql.append("   AND B.REQUEST_DATE(+) = TO_CHAR (SYSDATE, 'yyyyMMdd')    ");
			sql.append("   AND B.STATUS IN (0, 1) "			);    	
			sql.append("   AND ROWNUM = 1"					);
	        
			pstmt = conn.prepareStatement(sql.toString());
			pstmt.setString(1, item_no);
			pstmt.setInt(2, this._i_store_code);
			rs = pstmt.executeQuery();
			
			if(rs.next()){	    				
				return true;
			}
				
			return false;
			
	    } catch(Exception e) {
	    	return false;		      
		} finally {
			try{
		        if (rs != null)
		            rs.close();
		    } catch(Exception e){}
		    try{
		        if (pstmt != null)
		            pstmt.close();
		    } catch(Exception e){}
		}
	}
}
	
	

