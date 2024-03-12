/*****************************************************
 * PROGRAM ID    : PopSave
 * PROGRAM NAME	 : Price Card(등록)
 * CREATION DATE : 2013
 *****************************************************
 *****************************************************
 *  변경일자    /  변경자  / 변경사유
 *					  
 ******************************************************/

package tesco.got.dbTran;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import tesco.got.Common;
import awoo.dbUtil.ConnPool;
import awoo.dbUtil.OraConnFactory;
import awoo.util.StringUtil;

public class PopSave {
	private String _s_store_code = "";
	
    private Object[] _items = null;    
    
    public PopSave(String store_code,Object[] items){
    	this._s_store_code = store_code;    	
        this._items = items;
    }
    
    /**
     * @throws Exception
     */
    public void executeQuery() throws GotException,Exception{
        
        StringBuffer sql = null;        
        Connection conn = null;
        ConnPool cp = null;
        PreparedStatement pstmt = null;

        try{
            cp = ConnPool.getInstance();
            conn = cp.getConnection();
                        
            String item_no = "";
            String qty = "";
            String event_yn	= "";
            
            sql = new StringBuffer();
            sql.append("INSERT INTO PRICE_CARD  ");
            sql.append("( "				      	 );
            sql.append("	STORE_NO,"	      	 );
    		sql.append("    WORK_DATE,"		  	 );
    		sql.append("    IN_FLAG,"			 );
    		sql.append("    WORK_TIME,"		  	 );
    		sql.append("    ITEM_NO,"			 );
    		sql.append("    GROUP_NUMBER, "	  	 );
    		sql.append("    SEQ, "				 );
			sql.append("    ITEM_QTY,"           );
			sql.append("    SALES_CLASS_NO,"     );
			sql.append("    EVENT_NO, "          );
			sql.append("    EVENT_YN, "          );
			sql.append("    CONVERT_FLAG "       );
    		sql.append(") " 					 );
    		sql.append("SELECT ?, "					  			); // store_no
    		sql.append("       TO_CHAR(SYSDATE,'YYYYMMDD'),"    ); // work_date
    		sql.append("       '2',"							); // in_flag
    		sql.append("       TO_CHAR(SYSDATE,'HH24MISS'),"    ); // work_time
    		sql.append("       P.ITEM_NO,"						); // item_no
    		sql.append("       '',"								); // group_no
    		sql.append("       SEQ,"							); // seq
    		sql.append("       ?,"								); // item_qty
    		sql.append("       P.SALES_CLASS_NO,"				); // class_ref_no
    		sql.append("       P.EVENT_NO,"						); // event_no
    		sql.append("       P.EVENT_YN,"						); // event_yn
    		sql.append("       'N'"								); // convert_flag    		
			sql.append(" FROM ("													);
			sql.append("        SELECT ITEM_NO, "									);
			sql.append("               SALES_CLASS_NO, "							);
			sql.append("         	   EVENT_NO, "									);
			sql.append("         	   DECODE(PRICING_METHOD, 1, DECODE(LENGTH(PROM_EVENT_NO),10,'Y','N')");
			sql.append("				 				    , 4,'Y') EVENT_YN,"				);
			sql.append("          	   NVL((SELECT MAX(SEQ) + 1 "							);
			sql.append("          		      FROM PRICE_CARD "								);
			sql.append("				     WHERE STORE_NO  = A.STORE_NO "				   	);
			sql.append("				       AND WORK_DATE = TO_CHAR(SYSDATE,'yyyymmdd')"	);
			sql.append("				    ), 1) SEQ "										);
			sql.append("          FROM PLU A"												);
			sql.append("         WHERE ITEM_NO  = ?"										);
			sql.append("		   AND STORE_NO = ?"									    );
			sql.append("	   ) P"															);
            
            pstmt = conn.prepareStatement(sql.toString());

            for(int i = 3; i < this._items.length; i++){

                Object[] tmp = StringUtil.split(this._items[i].toString(),Common.GS);
                
                item_no   = tmp[0].toString();
                qty		  = tmp[1].toString();               
                event_yn  = tmp[2].toString();                
        		
                pstmt.setString(1, _s_store_code);
                pstmt.setString(2, qty);
                pstmt.setString(3, item_no);
                pstmt.setString(4, _s_store_code);
                               
        		pstmt.executeUpdate();
            }         
            
            pstmt.close();
        } 
        catch(SQLException se){
        	try
        	{
        		conn.rollback();
        		conn.close();
        	}
        	catch (Exception e) {}        	
        	conn = OraConnFactory.getInstance().getConnection();
        	throw se;
        }
        catch(Exception e){
        	throw e;
        }
        finally {            
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
