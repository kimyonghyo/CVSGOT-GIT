/*****************************************************
 * PROGRAM ID    : IodSave
 * PROGRAM NAME  : 통합발주 저장부분
 * CREATION DATE : 2013
 * *  2014-06-27 / 김재범/ 발주가능시간 조정
 *****************************************************/ 
package tesco.got.dbTran;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import awoo.dbUtil.ConnPool;
import awoo.dbUtil.OraConnFactory;

public class IodSave_20141031 {

	private String _s_store_code = "";
	private int    _i_store_code = 0;
	private String _barcode      = "";	
	private String _qty 		 = "";
	private String _time_flag 	 = "";
	private String _order_method = "";
	private String _user_id 	 = "";
	
    public IodSave_20141031(String store_code, String barcode, String qty, String time_flag, String order_method, String user_id) {
    	this._s_store_code = store_code;
    	this._i_store_code = Integer.parseInt(store_code);    	
        this._barcode      = barcode;  
    	this._qty		   = qty;
    	this._time_flag    = time_flag;
    	this._order_method = order_method;
    	this._user_id	   = user_id;
    	
    }    
	/** 
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

        String orderFalse 		= "";
        String last_order_time 	= "";
        String day_check 		= "";
        String orderDate 		= "";
        String order_time_flag  = "";
        String item_code		= "";
	      String m_hns_store = "0";	/* Hub Spoke 관련 추가 - HUB SPOKE 점포 여부 '1'*/        
        /*담배 발주량 제한 로직 추가*/
	      String section			="";
        int ordAvgQty =0; /*담배 평균 발주량*/
        int ordQty = 0;/*담배 금일 + 익일 발주량*/
        int ordRecQty = 0;/*담배당월 매입량+ 입고예정량 */
        
		try {
			cp = ConnPool.getInstance();
			conn = cp.getConnection();
			
            sql = new StringBuffer();
            sql.append("SELECT TO_CHAR(MAX(TO_DATE(SSMTIME,'HH24:MI'))+30/1440,'HH24:MI') AS LAST_ORDER_TIME");
            sql.append("  FROM STORE_SECTION_ORDERTIME");
            
            pstmt = conn.prepareStatement(sql.toString());            
            rs = pstmt.executeQuery();
            
            if(rs.next()){
            	last_order_time	= rs.getString("LAST_ORDER_TIME");	
            }
            
            rs.close();
            pstmt.close();
            
            /*담배 발주제한 체크*/
            
            sql = new StringBuffer();
            sql.append("SELECT SUBSTR(A.SALES_CLASS_NO,1,4) AS SECTION_CD ");
            sql.append("  FROM PLU A ");
            sql.append("   WHERE A.STORE_NO  = ?");
            sql.append("   AND A.ITEM_NO = ?");     
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setInt(1, this._i_store_code);
            pstmt.setString(2, this._barcode); 
            rs = pstmt.executeQuery();
            
            
            
            if(rs.next()){
            	section = rs.getString("SECTION_CD");	
            	//throw new GotException(section + " " +this._s_store_code);
            }
            
            
            rs.close();
            pstmt.close();
            
            
            if(section.equals("3301"))
            {
            	sql = new StringBuffer();
            	//sql.append("  SELECT 6570 AS AVGQTY, 5460 AS RECQTY, 840 AS ORDQTY  ");
	            sql.append("SELECT FNC_GET_AVG_QTY(A.STORE_NO) AS AVGQTY, FNC_GET_REC_QTY(A.STORE_NO) AS RECQTY,FNC_GET_ORD_QTY(A.STORE_NO) AS ORDQTY ");
	            sql.append("  FROM PLU A");
	            sql.append("   WHERE A.STORE_NO  = ?");
	            sql.append("   AND A.ITEM_NO = ?");     
	            pstmt = conn.prepareStatement(sql.toString());
	            pstmt.setInt(1, this._i_store_code);
	            pstmt.setString(2, this._barcode); 
	            rs = pstmt.executeQuery();
	            
	            if(rs.next()){
	            	
	            	ordAvgQty	= rs.getInt("AVGQTY");	
	            	ordRecQty	= rs.getInt("RECQTY");
	            	ordQty	= rs.getInt("ORDQTY");
	            	
	            }else{
	            	throw new GotException("발주 가능한 상품이 아닙니다(담배).\n본사로 연락 부탁 드립니다.");
	            }
	            
	            if(ordAvgQty - (ordRecQty +ordQty) < Integer.parseInt(this._qty))
	            {
	            	throw new GotException("월 평균 매입 수량 :"+Integer.toString(ordAvgQty)+" 개\n당월 매입 수량(입고예정포함):  "+Integer.toString(ordRecQty)+" 개\n당일 발주 수량(익일 발주 포함):"+Integer.toString(ordQty)+" 개\n추가 발주 가능 수량: "+Integer.toString(ordAvgQty - (ordRecQty +ordQty))+" 개 입니다");
	            }
	            
	            rs.close();
	            pstmt.close();
	           
            }
                              
            sql = new StringBuffer();
//            sql.append("SELECT CASE WHEN TO_CHAR(SYSDATE,'HH24MI') BETWEEN '0700' AND '0800' THEN 'F'"		);
            sql.append("SELECT CASE WHEN TO_CHAR(SYSDATE,'HH24MI') BETWEEN '0700' AND '0730' THEN 'F'"		);
            sql.append("       		ELSE 'T'"																);
            sql.append("       	END ORDER_FALSE, "															);    
            sql.append("   	   CASE WHEN TO_CHAR(SYSDATE,'HH24MI') BETWEEN '0000' AND '0700' OR TO_CHAR(SYSDATE,'HH24:MI') > '" + last_order_time + "' THEN 'DD'");            
            sql.append("       		ELSE 'D'"																);
            sql.append("       	END DAY_CHECK,"																);
            sql.append("       CASE WHEN TO_CHAR(SYSDATE,'HH24MI') BETWEEN '0000' AND '0700' THEN TO_CHAR(SYSDATE,'yyyymmdd')"	);
            sql.append("       		ELSE TO_CHAR(SYSDATE+1,'yyyymmdd')"														  	);
            sql.append("       	END ORDER_DATE,"																				);            
            sql.append("       CASE WHEN TO_CHAR(TRUNC(SYSDATE, 'MI'), 'HH24:MI') > SSMTIME THEN 'O'"							);
            sql.append("            WHEN TO_CHAR(SYSDATE,'HH24MI') BETWEEN '0000' AND '0700' THEN 'O'"							);
            sql.append("       		ELSE 'U'"																					);
            sql.append("       	END ORDER_TIME_FLAG,"																			);
            sql.append("       B.ITEM_NO,"																						);
/* Hub Spoke 관련 추가 - Start */            
            sql.append("       NVL(TO_CHAR((SELECT COUNT(*) FROM MASTER_MNC WHERE STORE_NO = B.STORE_NO AND ROWNUM = 1)),'0') HNS_STORE ");            
/* Hub Spoke 관련 추가 - End */            
            sql.append("  FROM STORE_SECTION_ORDERTIME A, PLU B "																);
            sql.append(" WHERE A.SECTION_CD  = SUBSTR(B.SALES_CLASS_NO, 0, 4)"													);
            sql.append("   AND B.STORE_NO    = ? "																				);            
            sql.append("   AND B.ITEM_NO 	 = ? "																				);            
//            sql.append(" GROUP BY A.SSMTIME"																					);
            
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setInt(1, this._i_store_code);
            pstmt.setString(2, this._barcode);            
            
            rs = pstmt.executeQuery();
            
            if(rs.next()){
            	item_code		= rs.getString("ITEM_NO");
            	orderFalse 		= rs.getString("ORDER_FALSE");
            	day_check		= rs.getString("DAY_CHECK");
            	orderDate		= rs.getString("ORDER_DATE");
            	order_time_flag = rs.getString("ORDER_TIME_FLAG");
/* Hub Spoke 관련 추가 - Start */
              m_hns_store	 = rs.getString("HNS_STORE");            	              	            
/* Hub Spoke 관련 추가 - End */              
            }else{
            	throw new GotException("발주 시간 설정이 되지 않은 Section입니다.\n본사로 연락 부탁 드립니다.");
            }
            
            rs.close();
            pstmt.close();
            
            if(orderFalse.equalsIgnoreCase("F")){
//            	throw new GotException("발주 불가능한 시간 입니다.\nAM 07:00~08:00 발주 리스트 생성 시간입니다.");
               	throw new GotException("발주 불가능한 시간 입니다.\nAM 07:00~07:30 발주 리스트 생성 시간입니다.");
                           }
            
            if(order_time_flag.equalsIgnoreCase("O")){
            	if(day_check.equalsIgnoreCase("D")){
            		throw new GotException("발주 마감 되었습니다.\n발주 가능시간은 "+ last_order_time +" 입니다.");
            	}            	
            }
            
            if(this._time_flag.equalsIgnoreCase("O")){
            	//발주시간이 마감 되었다면
    			sql = new StringBuffer();
    			sql.append("SELECT COUNT(ITEM_NO)  ");
/* Hub Spoke 관련 추가 - Start */            
          if(m_hns_store.equalsIgnoreCase("1")){                
              sql.append("  FROM ORDER_MANUAL_FU_MNC ");
          }else{
              sql.append("  FROM ORDER_MANUAL_FU ");            	
          }
/* Hub Spoke 관련 추가 - End */                            			
    			sql.append(" WHERE STORE_NO     = ?");
    			sql.append("   AND ITEM_NO      = ?");
    			sql.append("   AND ORDER_DATE   = TO_CHAR(SYSDATE+1,'YYYYMMDD')");
    			sql.append("   AND ORDER_METHOD = 'A'"							);
    			sql.append("   AND ITEM_NO NOT IN (SELECT ITEM_NO "				);
    			sql.append("       					 FROM PROMOTION_ITEM_V"		);
    			sql.append("       					WHERE STORE_NO = ?"			);
    			sql.append("           				  AND ITEM_NO  = ?"			);
    			sql.append("           				  AND TO_CHAR(SYSDATE+1,'yyyymmdd') BETWEEN TO_CHAR(TO_DATE(STARTDATE,'yyyymmdd')-2,'yyyymmdd')");
    			sql.append("           					 									AND TO_CHAR(TO_DATE(ENDDATE,'yyyymmdd')-2,'yyyymmdd')) ");
    			
    			pstmt = conn.prepareStatement(sql.toString());
    			
    			pstmt.setInt(1, this._i_store_code); 
    			pstmt.setString(2, item_code);
    			pstmt.setInt(3, this._i_store_code); 
    			pstmt.setString(4, item_code);
    			
    			rs = pstmt.executeQuery();
    			
    			if(rs.next()&&rs.getInt(1)!=0){
    				throw new GotException("자동 발주 상품 입니다.\n발주 등록 할 수 없습니다. : " + this._barcode);
    			}
         		
         		pstmt.close();
         		rs.close();

            	sql = new StringBuffer();
/* Hub Spoke 관련 추가 - Start */            
          		if(m_hns_store.equalsIgnoreCase("1")){                
          		    sql.append("UPDATE ORDER_MANUAL_FU_MNC ");
          		}else{
          		    sql.append("UPDATE ORDER_MANUAL_FU ");            	
          		}
/* Hub Spoke 관련 추가 - End */                            			            	
            	sql.append("   SET ORDER_QTY    = TO_NUMBER(TRIM(?))"	);	
            	sql.append("     , CREATE_DATE  = SYSDATE"				);
            	sql.append("     , CREATOR      = ?"					);	
            	sql.append("     , STATUS       = 2"					);
            	sql.append("     , WRITE_METHOD = 'M'"					);
            	sql.append(" WHERE STORE_NO   = ?"						);
            	sql.append("   AND ORDER_DATE = ?"						);
            	sql.append("   AND ITEM_NO    = ?"						);             	
            	sql.append("   AND STATUS IN ('1','2')    ");                
            	                                       
                pstmt = conn.prepareStatement(sql.toString());
                
                pstmt.setString(1, this._qty);
                pstmt.setString(2, this._user_id);
                pstmt.setInt(3, this._i_store_code); 
                pstmt.setString(4, orderDate);
                pstmt.setString(5, item_code);
                
         		
         		if(pstmt.executeUpdate()<=0){
         			throw new GotException("발주 등록을  할수 없습니다 : " + this._barcode);
            	}
         		
         		pstmt.close();         		         		   
                 
    		}else{
    			//발주시간 이내 라면 추가발주    			
    			sql = new StringBuffer();
    			sql.append("SELECT COUNT(ITEM_NO)    ");
/* Hub Spoke 관련 추가 - Start */            
          if(m_hns_store.equalsIgnoreCase("1")){                
              sql.append("  FROM ORDER_MANUAL_MNC ");
          }else{
              sql.append("  FROM ORDER_MANUAL ");            	
          }
/* Hub Spoke 관련 추가 - End */                            			
    			sql.append(" WHERE STORE_NO     = ?  ");
    			sql.append("   AND ITEM_NO      = ?  ");
    			sql.append("   AND ORDER_DATE   = TO_CHAR(SYSDATE,'YYYYMMDD')   ");
    			sql.append("   AND ORDER_METHOD = 'A'");
    			sql.append("   AND ITEM_NO NOT IN (SELECT ITEM_NO");
    			sql.append("       					 FROM PROMOTION_ITEM_V");
    			sql.append("       				 	WHERE STORE_NO = ?");
    			sql.append("           				  AND ITEM_NO  = ?");
    			sql.append("           				  AND TO_CHAR(SYSDATE,'yyyymmdd') BETWEEN TO_CHAR(TO_DATE(STARTDATE,'yyyymmdd')-2,'yyyymmdd')");
    			sql.append("           		 											  AND TO_CHAR(TO_DATE(ENDDATE,'yyyymmdd')-2,'yyyymmdd')) ");
    			
    			pstmt = conn.prepareStatement(sql.toString());
    			pstmt.setInt(1, this._i_store_code);
    			pstmt.setString(2, item_code);
    			pstmt.setInt(3, this._i_store_code);
    			pstmt.setString(4, item_code);
    			
    			rs = pstmt.executeQuery();
    			
    			if(rs.next()&&rs.getInt(1)!=0){
    				throw new GotException("자동 발주 상품 입니다.\n발주 등록 할 수 없습니다. : " + this._barcode);
    			}
    			
    			rs.close();
         		pstmt.close();         		
            		
    			sql = new StringBuffer(); 
/* Hub Spoke 관련 추가 - Start */            
          if(m_hns_store.equalsIgnoreCase("1")){                
              sql.append("UPDATE ORDER_MANUAL_MNC ");
          }else{
              sql.append("UPDATE ORDER_MANUAL ");            	
          }
/* Hub Spoke 관련 추가 - End */                            			            	    			
    			sql.append("   SET ORDER_QTY    = TO_NUMBER(TRIM(?))"	);
    			sql.append("     , CREATE_DATE  = SYSDATE"				);
    			sql.append("     , CREATOR      = ?"					);
    			sql.append("     , STATUS       = 2"					);
    			sql.append("     , WRITE_METHOD = 'M'"					);
    			sql.append(" WHERE STORE_NO     = ?"					);    			
    			sql.append("   AND ITEM_NO      = ?"					); 
    			sql.append("   AND ORDER_DATE 	= TO_CHAR(SYSDATE,'yyyymmdd')");    			
    			sql.append("   AND STATUS IN ('1','2')");                
    			                                      
                pstmt = conn.prepareStatement(sql.toString());
                
                pstmt.setString(1, this._qty);
                pstmt.setString(2, this._user_id);
                pstmt.setInt(3, this._i_store_code);
                pstmt.setString(4, item_code);
         		              		
         		if(pstmt.executeUpdate()<=0){
            		throw new GotException("발주 등록을  할수 없습니다 : " + this._barcode);
            	}
         		
         		pstmt.close();              	
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
	
	

