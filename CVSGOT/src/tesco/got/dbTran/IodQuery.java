/*****************************************************
 * PROGRAM ID    : IodQuery
 * PROGRAM NAME	 : 통합발주 아이템조회
 * CREATION DATE : 2013
 *****************************************************
 *****************************************************
 *  변경일자   /  변경자 / 변경사유  
 *  2014-05-07 / 김진승 / Hub & Spoke 관련 수정
 *  2014-06-27 / 김재범/ 발주가능시간 조정
 *  2015-03-18 / 이종욱/ 발주생성 시간 변경(기존 07:00 -> 05:00)에 따른 시간 변경
 ******************************************************/  

package tesco.got.dbTran;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import tesco.got.Common;
import awoo.dbUtil.ConnPool;
import awoo.dbUtil.OraConnFactory;
import awoo.util.Logger;

public class IodQuery {
	private String _s_store_code = "";
	private int    _i_store_code = 0;
	private String _barcode      = "";


    /**
     * @param store_code
     * @param item_code
     */
    public IodQuery(String store_code, String barcode) {
    	this._s_store_code = store_code;
    	this._i_store_code = Integer.parseInt(store_code);    	
        this._barcode      = barcode;    
    }
    
    public String executeQuery(Logger logger) throws GotException,Exception{
    	StringBuffer sql = null;    	
        Connection conn = null;
        ConnPool cp = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        String normal_price = "";
        String order_method = "";
        String order_type = "";
        String item_no	= "";
        String short_descr = "";            
        String rscode = "";
        String section_cd = "";
        String status = "";
        String repl_Check = "";
        String repl_day = "";
        String last_order_time = "";
        String orderFalse = "";
        String sup_order_Check = "";
        String supplier_no = "";
        String leadtime = "";
        String order_cat = "";
        String w_in_date = "";
        String in_date = "";
        String order_true_day = "";
        String day_check = "";            
        String orderDate = "";
        String sectionCheck = "";
        String order_time_flag = "";
    	String order_time = "";            
        String receipt_date = "";

        int in_1 = 0;
        int in_2 = 0;            
        String stock_on_hand	= "";
        String aso = "";                
        int pack_size = 0;
        int order_qty = 0;
        String auto_order_qty = "0";;
        int roq	= 0;
        String PromotionType = "";
        String PromotionName = "";
        String StartDate = "";
        String EndDate = "";
        String AutoView = "";
	    String m_hns_store = "0";	/* Hub Spoke 관련 추가 - HUB SPOKE 점포 여부 '1'*/
	    String m_hq_item = "0";	/* Hub Spoke 관련 추가 - '1' 본사발주 상품*/
	    String unavailable_item = "";		// 2015-06-02 추가. 이종욱. 장기미납 등 특정 activate_date 코드 상품은 발주 불가 여부 체크
        
                
        try{
            cp = ConnPool.getInstance();
            conn = cp.getConnection();
                  
            sql = new StringBuffer();           
/* Hub Spoke 관련 변경            
            sql.append("SELECT A.ITEM_NO,");
            sql.append("       B.SHORT_DESCR,");
            sql.append("   	   CASE WHEN A.STATUS IN (0,2) THEN '1'   ");
            sql.append("   		    WHEN A.PRIMARY_PACK_NO = A.ITEM_NO THEN '2'");
            sql.append("   			ELSE '0'");
            sql.append("   		END AS RSCODE,"); 
            sql.append("   	   SUBSTR(A.SALES_CLASS_NO,1,4) AS SECTION_CD, ");
            sql.append("       ROUND(DECODE(A.PRICING_METHOD,4,NVL(A.EVENT_PRICE,A.NORMAL_PRICE),NVL(A.NORMAL_PRICE,0))+ITEM_REAL_PRICE(A.ITEM_NO, A.STORE_NO),0) NORMAL_PRICE ");            
            sql.append("  FROM PLU A, ITEM B");
            sql.append(" WHERE A.ITEM_NO  = B.ITEM_NO");
            sql.append("   AND A.ITEM_NO  = ?");
            sql.append("   AND A.STORE_NO = ?");
*/
            sql.append("SELECT A.ITEM_NO,");
            sql.append("       B.SHORT_DESCR,");
            sql.append("   	   CASE WHEN A.STATUS IN (0,2) THEN '1'   ");
            sql.append("   		    WHEN A.PRIMARY_PACK_NO = A.ITEM_NO THEN '2'");
            sql.append("   			ELSE '0'");
            sql.append("   		END AS RSCODE,"); 
            sql.append("   	   SUBSTR(A.SALES_CLASS_NO,1,4) AS SECTION_CD, ");
            sql.append("       ROUND(DECODE(A.PRICING_METHOD,4,NVL(A.EVENT_PRICE,A.NORMAL_PRICE),NVL(A.NORMAL_PRICE,0))+ITEM_REAL_PRICE(A.ITEM_NO, A.STORE_NO),0) NORMAL_PRICE, ");            
            sql.append("       NVL(TO_CHAR((SELECT COUNT(*) FROM MASTER_MNC WHERE STORE_NO = A.STORE_NO AND ROWNUM = 1)),'0') HNS_STORE, ");                                    
            sql.append("       NVL(DECODE(A.ACTIVATE_DATE,TO_DATE('2100-12-31','YYYY-MM-DD'),DECODE(A.REPL_ORDER_CTRL,'A','1','0'),'0'),'0') HQ_ITEM, ");
            sql.append("       CASE WHEN ISDATE(A.ACTIVATE_DATE) = 0 THEN 'FALSE' ");
            sql.append("                   WHEN TO_CHAR(A.ACTIVATE_DATE, 'YYYY-MM-DD') IN ('2050-12-31', '2080-12-31', '2088-12-31')  ");	
            sql.append("       			THEN 'TRUE' ELSE 'FALSE'  ");
            sql.append("       END AS UNAVAILABLE_ITEM  ");	// 2015-06-02 추가. 이종욱. 장기미납 등 특정 activate_date 코드 상품은 발주 불가 여부 체크
            sql.append("  FROM PLU A, ITEM B");
            sql.append(" WHERE A.ITEM_NO  = B.ITEM_NO");
            sql.append("   AND A.ITEM_NO  = ?");
            sql.append("   AND A.STORE_NO = ?");            
            
            pstmt = conn.prepareStatement(sql.toString());            
            pstmt.setString(1, _barcode);
            pstmt.setInt(2, this._i_store_code);
            
            rs = pstmt.executeQuery();
            
            if (!rs.next()){
            	throw new GotException("존재하지 않는 상품입니다 : " + this._barcode);            	
            }
            
            item_no		 = rs.getString("ITEM_NO");
            short_descr	 = rs.getString("SHORT_DESCR");
            rscode 		 = rs.getString("RSCODE");
            section_cd	 = rs.getString("SECTION_CD");
            normal_price = rs.getString("NORMAL_PRICE");
/* Hub Spoke 관련 추가 - Start */
            m_hns_store	 = rs.getString("HNS_STORE");
            m_hq_item = rs.getString("HQ_ITEM");            
/* Hub Spoke 관련 추가 - End */
            unavailable_item = rs.getString("UNAVAILABLE_ITEM");
            
            rs.close();
            pstmt.close();
            
            if(rscode.equalsIgnoreCase("1")){
            	throw new GotException("활성화 되지 않은 상품이라 발주 불가능 합니다 : " + this._barcode);
            }            
            else if(rscode.equalsIgnoreCase("2")){
            	throw new GotException("심플팩 코드입력불가. 컴포넌트 코드로 발주 등록 하세요");
            }
            
/* Hub Spoke 관련 추가 - Start */            
            if(m_hns_store.equalsIgnoreCase("1") && m_hq_item.equalsIgnoreCase("1") ){
            	throw new GotException("Hyper 상품 중 본사 발주 상품이라 발주 불가능 합니다 : " + this._barcode);
            }                        
/* Hub Spoke 관련 추가 - End */
            
            // 2015-06-02 추가. 이종욱. 장기미납 등 특정 activate_date 코드 상품은 발주 불가 여부 체크
            if(unavailable_item.equalsIgnoreCase("TRUE"))
            {
            	throw new GotException("발주 불가능 상품입니다 : " + this._barcode);
            }
            
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
            
            /*
             * 2015-03-18 수정. 이종욱. 발주생성 시간 변경(기존 07:00 -> 05:00)에 따른 시간 변경
             */
            sql = new StringBuffer();
//            sql.append("SELECT CASE WHEN TO_CHAR(SYSDATE,'HH24MI') BETWEEN '0700' AND '0800' THEN 'F'"	);
            sql.append("SELECT CASE WHEN TO_CHAR(SYSDATE,'HH24MI') BETWEEN '0500' AND '0530' THEN 'F'"	);
            sql.append("       		ELSE 'T' END ORDER_FALSE,"											);            
            sql.append("   	   CASE WHEN TO_CHAR(SYSDATE,'HH24MI') BETWEEN '0000' AND '0500' OR TO_CHAR(SYSDATE,'HH24:MI') > '" + last_order_time + "' THEN 'DD'"	  );            
            sql.append("       		ELSE 'D' END DAY_CHECK,"																										  );
            sql.append("   	   CASE WHEN TO_CHAR(SYSDATE,'HH24MI') BETWEEN '0000' AND '0500' OR TO_CHAR(SYSDATE,'HH24:MI') < SSMTIME THEN TO_CHAR(SYSDATE,'yyyymmdd')");
            sql.append("       		ELSE TO_CHAR(SYSDATE+1,'yyyymmdd') "								);
            sql.append("       	END ORDER_DATE,"														);
            sql.append("       NVL(SSMTIME,0) AS ORDER_TIME,"											);
            sql.append("       CASE WHEN TO_CHAR(TRUNC(SYSDATE, 'MI'), 'HH24:MI') > SSMTIME  THEN 'O' "	);		// 설정된 발주 제한시간
            sql.append("    		WHEN TO_CHAR(SYSDATE,'HH24MI') BETWEEN '0000' AND '0500' THEN 'O'"	);		// 새벽 제한 발주시간
            sql.append("    		ELSE 'U'"			);
            sql.append("    	END ORDER_TIME_FLAG"	);
            sql.append("  FROM STORE_SECTION_ORDERTIME"	);
            sql.append(" WHERE SECTION_CD = ?"			);
            sql.append(" GROUP BY SSMTIME"				);
            
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setString(1, section_cd);
            
            rs = pstmt.executeQuery();
            
            if(rs.next()){            	
            	orderFalse 		= rs.getString("ORDER_FALSE");
            	day_check		= rs.getString("DAY_CHECK");
            	orderDate		= rs.getString("ORDER_DATE");
            	order_time 		= rs.getString("ORDER_TIME");
            	order_time_flag = rs.getString("ORDER_TIME_FLAG");            	
            }else{
            	throw new GotException("발주 시간 설정이 되지 않은 Section입니다.\n본사로 연락 부탁 드립니다");
            }
            
            rs.close();
            pstmt.close();

            if(orderFalse.equalsIgnoreCase("F")){
//            	throw new GotException("발주 불가능한 시간 입니다.\nAM 07:00~08:00 발주 리스트 생성 시간입니다");
            	throw new GotException("발주 불가능한 시간 입니다.\nAM 05:00~05:30 발주 리스트 생성 시간입니다");
            }

            if(order_time_flag.equalsIgnoreCase("O")){
            	if(day_check.equalsIgnoreCase("D")){
            		throw new GotException("발주 마감 되었습니다.\n발주 가능시간은 "+ last_order_time +" 입니다");
            	}            	
            }
            
        	sql = new StringBuffer();
        	sql.append("SELECT PROMOTION_TYPE1, PROMOTION_TYPE2 ,PROMOTION_GROUP, TO_CHAR(TO_DATE(STARTDATE, 'yyyy-MM-dd'), 'yyyy-MM-dd') STARTDATE, TO_CHAR(TO_DATE(ENDDATE, 'yyyy-MM-dd'), 'yyyy-MM-dd') ENDDATE");
        	sql.append("  FROM PROMOTION_ITEM_V"	);
        	sql.append(" WHERE STORE_NO = ?"		);
        	sql.append("   AND ITEM_NO  = ?"		);
        	sql.append("   AND TO_CHAR(TO_DATE('" + orderDate + "','yyyymmdd'),'yyyymmdd') BETWEEN TO_CHAR(TO_DATE(STARTDATE,'yyyymmdd')-2,'yyyymmdd')");
        	sql.append("       																   AND TO_CHAR(TO_DATE(ENDDATE,'yyyymmdd')-2,'yyyymmdd')   ");
            
        	pstmt = conn.prepareStatement(sql.toString());
            pstmt.setInt(1, this._i_store_code);
            pstmt.setString(2, item_no);

            rs = pstmt.executeQuery();
            
            if(rs.next()){
            	order_method   = "P";
                PromotionType  = rs.getString("PROMOTION_TYPE1");
                PromotionName  = rs.getString("PROMOTION_GROUP");
                StartDate      = rs.getString("STARTDATE");
                EndDate        = rs.getString("ENDDATE");                	
            }
            
            rs.close();
            pstmt.close();
                       
            // 발주시간 마감 후
            if(order_time_flag.equalsIgnoreCase("O") || day_check.equalsIgnoreCase("DD")){         
            	
            	//logger.writeEntry("조회 분기 - 발주시간 마감");
            	
            	sql = new StringBuffer();
                sql.append("SELECT /*+ rule */"											);
                sql.append("       CASE WHEN ? = 'P' THEN 'P'"							);
                sql.append("          	WHEN SUBSTR(A.SECTION_CD,0,1) =  '1'  OR A.SECTION_CD IN ('2305','2306') THEN 'F'  ");
                sql.append("          	WHEN SUBSTR(A.SECTION_CD,0,1) <> '1' AND A.ORDER_METHOD = 'M' THEN 'G'"				);
               //2015-01-12 자동 발주 공산품 추가
                sql.append("          WHEN FNC_ITEM_ORDERABLE(A.STORE_NO,A.ITEM_NO) = 'Y' THEN 'G'"                                        ); 
                
                sql.append("        	ELSE A.ORDER_METHOD"	);
                sql.append("        END ORDER_METHOD,"			);
                sql.append("       A.STATUS,"		);
                sql.append("       A.ORDER_CAT,      ");
                sql.append("       CASE WHEN A.SUP_ORDER_DATE <> A.ORDER_DATE THEN '1'  ");
                sql.append("          	ELSE '0'");
                sql.append("      	END SUP_ORDER_CHECK, ");                
                sql.append("       (SELECT SUM(ON_HAND_QTY) FROM STOCK_ON_HAND WHERE STORE_NO = A.STORE_NO AND ITEM_No=A.ITEM_NO AND LOC_NO IN ('20','40'))  AS ON_HAND_QTY,");
                sql.append("       (SELECT SUM(ON_HAND_QTY) FROM STOCK_ON_HAND WHERE STORE_NO = A.STORE_NO AND ITEM_NO=A.ITEM_NO AND LOC_NO='20') AS ASO,");           
//                sql.append("       A.RECEIPT_D AS TODAY,  ");
//                sql.append("       A.RECEIPT_D2 AS TOMORROW,  ");
                sql.append("       NVL(FNC_ON_ORDER_DAYS(A.STORE_NO, A.ITEM_NO, 'A'),  0) AS TODAY,  ");
                sql.append("       NVL(FNC_ON_ORDER_DAYS2(A.STORE_NO, A.ITEM_NO, 'A'), 0) AS TOMORROW,  ");
                sql.append("       NVL(A.PACK_SIZE, 0) AS PACK_SIZE,		       ");                
                sql.append("       NVL(A.ROQ, 0) AS ROQ,		  ");                                
                sql.append("       NVL(A.ORDER_QTY, 0) AS ORDER_EA,		  ");
                sql.append("       NVL(A.RECEIPT_DATE, ' ') AS IN_DATE,             ");            
                sql.append("       CASE WHEN A.STATUS = '2' THEN NVL(A.ORDER_QTY, 0)");
                sql.append("       		WHEN A.STATUS = '3' THEN -1");
                sql.append("       		ELSE 0 ");
                sql.append("       	END AS ORDER_QTY, ");                
                sql.append("       NVL(A.ATTR_QTY,0) AS AUTO_ORDER_QTY, ");                
                sql.append("       TO_DATE(A.RECEIPT_DATE, 'yyyy-MM-dd') AS RECEIPT_DATE, ");
    			     // 2014-12-03 자동발주 상품이지만 타점 POG상품인 경우 발주 가능 - 변경 라인                
               // sql.append("       CASE WHEN A.ORDER_METHOD = 'A' THEN 'Y' ELSE 'N' END AUTO_VIEW ");
                sql.append("       CASE WHEN A.ORDER_METHOD = 'A' THEN DECODE(FNC_ITEM_ORDERABLE(A.STORE_NO,A.ITEM_NO),'Y','N','Y') ELSE 'N' END AUTO_VIEW ");
               // 2014-12-03 자동발주 상품이지만 타점 POG상품인 경우 발주 가능 - 변경 라인                                              
/* Hub Spoke 관련 추가 - Start */            
            		if(m_hns_store.equalsIgnoreCase("1")){                
            		    sql.append("  FROM ORDER_MANUAL_FU_MNC A");
            		}else{
            		    sql.append("  FROM ORDER_MANUAL_FU A");            	
            		}
/* Hub Spoke 관련 추가 - End */                        
                sql.append(" WHERE A.STORE_NO   = ? ");
                sql.append("   AND A.ORDER_DATE = ? ");
                sql.append("   AND A.ITEM_NO 	= ? ");                
                sql.append("   AND A.STATUS IN ('1','2','3','6')  ");                
                                
                pstmt = conn.prepareStatement(sql.toString());
                
                pstmt.setString(1, order_method);
                pstmt.setInt(2, this._i_store_code);
                pstmt.setString(3, orderDate);
                pstmt.setString(4, item_no);                

                rs = pstmt.executeQuery();
                
                if(rs.next()){               	
                	order_type		= rs.getString("ORDER_METHOD");                
                    status			= rs.getString("STATUS");
                    order_cat		= rs.getString("ORDER_CAT");
                    sup_order_Check	= rs.getString("SUP_ORDER_CHECK");
                    stock_on_hand	= rs.getString("ON_HAND_QTY");
                    aso				= rs.getString("ASO");                
                    in_1			= rs.getInt("TODAY");
                    in_2			= rs.getInt("TOMORROW");
                    pack_size		= rs.getInt("PACK_SIZE");
                    roq				= rs.getInt("ROQ");
                    order_qty		= rs.getInt("ORDER_QTY");
                    auto_order_qty	= rs.getString("AUTO_ORDER_QTY");                    
                    receipt_date	= rs.getString("RECEIPT_DATE");
                    AutoView		= rs.getString("AUTO_VIEW");
                    
                }else{
                	throw new GotException("발주 불가능한 상품입니다 : " + this._barcode);                    
                }
                
                rs.close();
                pstmt.close();
                
            }
            else		// 발주시간 이내
            {
            	//logger.writeEntry("조회 분기 - 발주시간 이내");
            	
            	sql = new StringBuffer();
                sql.append("SELECT /*+ rule */   ");
                sql.append("       CASE WHEN ? = 'P' THEN 'P'    ");
                sql.append("          	WHEN SUBSTR(A.SECTION_CD,0,1)='1' OR A.SECTION_CD IN ('2305','2306') THEN 'F'");
                sql.append("          	WHEN SUBSTR(A.SECTION_CD,0,1)<>'1' AND A.ORDER_METHOD='M' THEN 'G' ");
                //2015-01-12 자동 발주 공산품 추가
                    sql.append("          WHEN FNC_ITEM_ORDERABLE(A.STORE_NO,A.ITEM_NO) = 'Y' THEN 'G'"                                        ); 
                  
                sql.append("       		ELSE A.ORDER_METHOD"); 
                sql.append("       	END ORDER_METHOD,");
                sql.append("       A.STATUS,");
                sql.append("       A.ORDER_CAT,");
                sql.append("      (SELECT SUM(ON_HAND_QTY) FROM STOCK_ON_HAND WHERE STORE_NO = A.STORE_NO AND ITEM_NO=A.ITEM_NO AND LOC_NO IN ('20','40'))  AS ON_HAND_QTY,");
                sql.append("      (SELECT SUM(ON_HAND_QTY) FROM STOCK_ON_HAND WHERE STORE_NO = A.STORE_NO AND ITEM_NO=A.ITEM_NO AND LOC_NO='20') AS ASO,");           
//                sql.append("       A.RECEIPT_D AS TODAY,  ");
//                sql.append("       A.RECEIPT_D2 AS TOMORROW,  ");
                sql.append("       NVL(FNC_ON_ORDER_DAYS(A.STORE_NO, A.ITEM_NO, 'A'),  0) AS TODAY,  ");
                sql.append("       NVL(FNC_ON_ORDER_DAYS2(A.STORE_NO, A.ITEM_NO, 'A'), 0) AS TOMORROW,  ");                
                sql.append("       NVL(A.PACK_SIZE, 0)	AS PACK_SIZE,");                
                sql.append("       NVL(A.ROQ, 0)	AS ROQ,");                  
                sql.append("       NVL(A.ORDER_QTY, 0)	AS ORDER_EA,");
                sql.append("       NVL(A.RECEIPT_DATE, ' ') AS IN_DATE,");            
                sql.append("       CASE WHEN A.STATUS = '2' THEN NVL(A.ORDER_QTY, 0)");
                sql.append("       		WHEN A.STATUS = '3' THEN -1");
                sql.append("     	    ELSE 0 ");
                sql.append("        END AS ORDER_QTY,");                
                sql.append("       NVL(A.ATTR_QTY,0) AS AUTO_ORDER_QTY,");
                sql.append("       TO_DATE(A.RECEIPT_DATE, 'yyyy-MM-dd') AS RECEIPT_DATE,");
    			     // 2014-12-03 자동발주 상품이지만 타점 POG상품인 경우 발주 가능 - 변경 라인                
               // sql.append("       CASE WHEN A.ORDER_METHOD = 'A' THEN 'Y' ELSE 'N' END AUTO_VIEW ");
                sql.append("       CASE WHEN A.ORDER_METHOD = 'A' THEN DECODE(FNC_ITEM_ORDERABLE(A.STORE_NO,A.ITEM_NO),'Y','N','Y') ELSE 'N' END AUTO_VIEW ");
               // 2014-12-03 자동발주 상품이지만 타점 POG상품인 경우 발주 가능 - 변경 라인                
/* Hub Spoke 관련 추가 - Start */            
            		if(m_hns_store.equalsIgnoreCase("1")){                
            		    sql.append("  FROM ORDER_MANUAL_MNC A");
            		}else{
            		    sql.append("  FROM ORDER_MANUAL A");            	
            		}
/* Hub Spoke 관련 추가 - End */                                        
                sql.append(" WHERE A.STORE_NO   = ?");
                sql.append("   AND A.ITEM_NO    = ?");
                sql.append("   AND A.ORDER_DATE = TO_CHAR(SYSDATE,'yyyymmdd')");                                                
                sql.append("   AND A.STATUS IN ('1','2','3','6')  ");
                
                pstmt = conn.prepareStatement(sql.toString());                
                pstmt.setString(1, order_method);
                pstmt.setInt(2, this._i_store_code);
                pstmt.setString(3,item_no);                

                rs = pstmt.executeQuery();
                
                if(rs.next()){
                	order_type		= rs.getString("ORDER_METHOD");                
                    status			= rs.getString("STATUS");
                    order_cat		= rs.getString("ORDER_CAT");
                    sup_order_Check	= "0";
                    stock_on_hand	= rs.getString("ON_HAND_QTY");
                    aso				= rs.getString("ASO");                
                    in_1			= rs.getInt("TODAY");
                    in_2			= rs.getInt("TOMORROW");
                    pack_size		= rs.getInt("PACK_SIZE");
                    roq				= rs.getInt("ROQ");
                    order_qty		= rs.getInt("ORDER_QTY");
                    auto_order_qty	= rs.getString("AUTO_ORDER_QTY");                    
                    receipt_date	= rs.getString("RECEIPT_DATE");
                    AutoView		= rs.getString("AUTO_VIEW");
                }else{
                	throw new GotException("발주 불가능한 상품입니다 : " + this._barcode);
                }
                
                rs.close();
                pstmt.close();                                
            }
            
            if(!order_method.equals("P")) {
				if(AutoView.equalsIgnoreCase("Y")) {
					throw new GotException("자동 발주 설정된 아이템 입니다 : " + this._barcode);
				}                           
            }

            if(status.equalsIgnoreCase("4") || status.equalsIgnoreCase("5")){
            	throw new GotException("발주 불가능한 상태입니다 : " + this._barcode);
            }
            
            sql = new StringBuffer();
            sql.append("SELECT A.SUPPLIER_NO, ");
            sql.append("       LPAD(B.LEAD_TIME,2,'0') AS LEADTIME");
            sql.append("  FROM PLU A, SUPS B ");
            sql.append(" WHERE A.SUPPLIER_NO = B.SUPPLIER_NO");
            sql.append("   AND A.STORE_NO    = ?"		);
            sql.append("   AND A.ITEM_NO     = ?"		);            

            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setInt(1, this._i_store_code);
            pstmt.setString(2,item_no);
            
            rs = pstmt.executeQuery();
            
            if(rs.next()){            	
            	leadtime	= rs.getString("LEADTIME");            	
            	supplier_no	= rs.getString("SUPPLIER_NO");
            }
            
            rs.close();
            pstmt.close();
            
            sql = new StringBuffer();
            sql.append("SELECT O_MON||O_TUE||O_WED||O_THU||O_FRI||O_SAT||O_SUN AS REPL_DAY,");
            sql.append("       CASE WHEN TO_CHAR(TO_DATE('" + orderDate + "','yyyymmdd'),'D') = 1 AND O_SUN='Y' THEN '0'  ");
            sql.append("      		WHEN TO_CHAR(TO_DATE('" + orderDate + "','yyyymmdd'),'D') = 2 AND O_MON='Y' THEN '0'  ");
            sql.append("      		WHEN TO_CHAR(TO_DATE('" + orderDate + "','yyyymmdd'),'D') = 3 AND O_TUE='Y' THEN '0'  ");
            sql.append("      		WHEN TO_CHAR(TO_DATE('" + orderDate + "','yyyymmdd'),'D') = 4 AND O_WED='Y' THEN '0'  ");
            sql.append("      		WHEN TO_CHAR(TO_DATE('" + orderDate + "','yyyymmdd'),'D') = 5 AND O_THU='Y' THEN '0'  ");
            sql.append("      		WHEN TO_CHAR(TO_DATE('" + orderDate + "','yyyymmdd'),'D') = 6 AND O_FRI='Y' THEN '0'  ");
            sql.append("      		WHEN TO_CHAR(TO_DATE('" + orderDate + "','yyyymmdd'),'D') = 7 AND O_SAT='Y' THEN '0'  ");
            sql.append("      		ELSE '1'  ");
            sql.append("  		END AS REPL_CHECK  ");
            sql.append(" FROM REPL_DAY  ");
            sql.append("WHERE STORE_NO = ? ");
            sql.append("  AND ITEM_NO  = ? ");
            
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setInt(1, this._i_store_code);
            pstmt.setString(2, item_no);
           
            rs = pstmt.executeQuery();
            
            if(rs.next()){
            	repl_day = rs.getString("REPL_DAY");
            	repl_Check = rs.getString("REPL_CHECK");
            }
            
            rs.close();
            pstmt.close();

            sql = new StringBuffer();
            sql.append("SELECT TO_CHAR(FNC_STORE_ORDER_DATE(?, ?, TO_DATE(?, 'yyyymmdd') ),'yyyy-mm-dd') AS ORDER_DATE");
        	sql.append("  FROM DUAL  ");

            pstmt = conn.prepareStatement(sql.toString());            
            pstmt.setInt(1, this._i_store_code);            
            pstmt.setString(2, item_no);
            pstmt.setString(3, orderDate);
                        
            rs = pstmt.executeQuery();
            
            if(rs.next()){
            	order_true_day = rs.getString("ORDER_DATE");
            }
            
            rs.close();
            pstmt.close(); 

            if(sup_order_Check.equalsIgnoreCase("1") || repl_Check.equalsIgnoreCase("1") ){
            	throw new GotException("발주요일이 아닙니다.\n발주일: "+ GetWeekDays(repl_day).toString() + "요일\n발주가능일: " + order_true_day );
            }

            StringBuffer tmp = new StringBuffer();
            tmp.append(order_type);		//	발주타입
            tmp.append(Common.FS);
            tmp.append(item_no);				//	item_no
            tmp.append(Common.FS);
            tmp.append(short_descr);			//	상품명            
            tmp.append(Common.FS);
            tmp.append(normal_price);			//	가격            
            tmp.append(Common.FS);            
            tmp.append(repl_day);            
            tmp.append(Common.FS);
            tmp.append(leadtime);            
            tmp.append(Common.FS);
            tmp.append(order_time_flag);
            tmp.append(Common.FS);
            tmp.append(order_time);          
            tmp.append(Common.FS);            
            tmp.append(stock_on_hand);		//	전산재고            
            tmp.append(Common.FS);
            tmp.append(aso);					//	가용재고
            tmp.append(Common.FS);
            tmp.append(in_1);					//	입고예정1 
            tmp.append(Common.FS);
            tmp.append(in_2);					//	입고예정2
            tmp.append(Common.FS);
            tmp.append(pack_size);				//	pack_size(입수)
            tmp.append(Common.FS);
            tmp.append(roq);					//	roq            
            tmp.append(Common.FS);
            tmp.append(order_qty);			//	발주수량
            tmp.append(Common.FS);
            tmp.append(auto_order_qty);
            tmp.append(Common.FS);
            tmp.append(receipt_date);
            
            if(order_type.equalsIgnoreCase("G") || order_method.equalsIgnoreCase("P")){
            
                sql = new StringBuffer();
                sql.append("SELECT NVL(ROUND(SUM(SALE_WEIGHT)/(TRUNC(SYSDATE)-TO_DATE(MAX(LAST_SALE),'YYYYMMDD')),1),0) AS AVG");
                sql.append("  FROM (SELECT sale_date, item_no, sale_qty, sale_weight, MIN(SALE_DATE) OVER() AS LAST_SALE    ");
                sql.append("          FROM ITEM_SALES");
                sql.append("         WHERE store_no = ?");
                sql.append("           AND item_no  = trim(?)    ");
                sql.append("           AND sale_date BETWEEN TO_CHAR (SYSDATE - 14, 'YYYYMMDD')    ");
                sql.append("                             AND TO_CHAR (SYSDATE - 1, 'YYYYMMDD'))    ");

                pstmt = conn.prepareStatement(sql.toString());
                pstmt.setInt(1, this._i_store_code);
                pstmt.setString(2, item_no);
                rs = pstmt.executeQuery();
                
                if (rs.next()){                	
                	tmp.append(Common.FS);
                	tmp.append(Double.parseDouble(rs.getString("AVG")));
                }
           
                rs.close();
                pstmt.close();
                
                sql = new StringBuffer();
                
                sql.append("SELECT NVL(FNC_ON_ORDER(?, ?, 'A'), 0) AS ON_ORDER FROM DUAL");
                pstmt = conn.prepareStatement(sql.toString());
                
                pstmt.setInt(1, this._i_store_code);
                pstmt.setString(2,item_no);
                             
                rs = pstmt.executeQuery();
                
                if(rs.next()){
                	tmp.append(Common.FS);
                	tmp.append(rs.getString("ON_ORDER"));
                }
                
                rs.close();
                pstmt.close();

            }
            
            if( order_type.equalsIgnoreCase("F") || order_method.equalsIgnoreCase("P") ) {
                sql = new StringBuffer();
                sql.append("SELECT NVL(A.SALES_D1,0)||'/'||NVL(B.LOSS_D1,0) AS D1,");
                sql.append("  	   NVL(A.SALES_D2,0)||'/'||NVL(B.LOSS_D2,0) AS D2,");
                sql.append("       NVL(A.SALES_D3,0)||'/'||NVL(B.LOSS_D3,0) AS D3,");
                sql.append("       NVL(A.SALES_D4,0)||'/'||NVL(B.LOSS_D4,0) AS D4,");
                sql.append("       NVL(A.SALES_D5,0)||'/'||NVL(B.LOSS_D5,0) AS D5,");
                sql.append("       NVL(A.SALES_D6,0)||'/'||NVL(B.LOSS_D6,0) AS D6 ");
                sql.append("  FROM SALES_FOR_ORDER A "			);
                sql.append("  LEFT OUTER JOIN LOSS_FOR_ORDER B ");
                sql.append("  	ON A.STORE_NO   = B.STORE_NO"	);
                sql.append("   AND A.ITEM_NO    = B.ITEM_NO"	);
                sql.append("   AND A.ORDER_DATE = B.ORDER_DATE"	);
                sql.append(" WHERE A.STORE_NO   = ?"			);
                sql.append("   AND A.ITEM_NO    = ?"			);                                
                sql.append("   AND A.ORDER_DATE= TO_CHAR(SYSDATE,'yyyymmdd')  ");
                
                pstmt = conn.prepareStatement(sql.toString());
                pstmt.setInt(1, this._i_store_code);
                pstmt.setString(2, item_no);
                
                rs = pstmt.executeQuery();
                
                if(rs.next()){                	
                	tmp.append(Common.FS);            
                    tmp.append(rs.getString("D1"));						//	d-1
                    tmp.append(Common.FS);
                    tmp.append(rs.getString("D2"));						//	d-2
                    tmp.append(Common.FS);
                    tmp.append(rs.getString("D3"));						//	d-3
                    tmp.append(Common.FS);
                    tmp.append(rs.getString("D4"));						//	d-4
                    tmp.append(Common.FS);
                    tmp.append(rs.getString("D5"));						//	d-5
                    tmp.append(Common.FS);
                    tmp.append(rs.getString("D6"));						//	d-6                	
                }else{
                	tmp.append(Common.FS);            
                    tmp.append("0/0");
                    tmp.append(Common.FS);
                    tmp.append("0/0");
                    tmp.append(Common.FS);
                    tmp.append("0/0");
                    tmp.append(Common.FS);
                    tmp.append("0/0");
                    tmp.append(Common.FS);
                    tmp.append("0/0");
                    tmp.append(Common.FS);
                    tmp.append("0/0");                	
                }
                
                rs.close();
                pstmt.close();	            	
            }
            
            if(order_type.equalsIgnoreCase("F")){
                
                sql = new StringBuffer();
                sql.append("SELECT ( SELECT NVL(D_MON, 'N')||NVL(D_TUE, 'N')||NVL(D_WED, 'N')||  ");
                sql.append("       NVL(D_THU, 'N')||NVL(D_FRI, 'N')||NVL(D_SAT, 'N')|| NVL(D_SUN, 'N')  ");
                sql.append("       FROM SUPS_ATTR WHERE SUPPLIER_NO = TO_CHAR(AA.SUPPLIER_NO)  AND STORE_NO = TO_CHAR(AA.STORE_NO) ) AS IPGODAYS ");
                sql.append("  FROM PLU AA ");
                sql.append(" WHERE AA.STORE_NO    = ?   ");
                sql.append("   AND AA.ITEM_NO     = ?   ");                
                
                pstmt = conn.prepareStatement(sql.toString());
                pstmt.setInt(1, this._i_store_code);
                pstmt.setString(2, item_no);                
                
                rs = pstmt.executeQuery();

                if(rs.next()){                	
                	tmp.append(Common.FS);
                    tmp.append(rs.getString("IPGODAYS"));      	
                }else{
                	throw new GotException("입고정보가 없습니다.\n본사로 연락 부탁드립니다");
                }

                rs.close();
                pstmt.close();                
            }
            
            if(order_type.equalsIgnoreCase("P") ){            
            	tmp.append(Common.FS);
            	tmp.append(PromotionType);
            	tmp.append(Common.FS);
            	tmp.append(PromotionName);
            	tmp.append(Common.FS);
            	tmp.append(StartDate);
            	tmp.append(Common.FS);
            	tmp.append(EndDate);     
                
                rs.close();
                pstmt.close();                
            }
            
            tmp.append(Common.FS);
            
            sql = new StringBuffer();
			sql.append("SELECT ROUND((SUM(SALE_QTY)/28)*3, 0) AVG_SALE_QTY");
			sql.append("  FROM ITEM_SALES");
			sql.append(" WHERE SALE_DATE BETWEEN TO_CHAR(SYSDATE-28, 'YYYYMMDD') AND TO_CHAR(SYSDATE, 'yyyyMMdd')");
			sql.append("   AND STORE_NO = ?");
			sql.append("   AND ITEM_NO  = ?");
			sql.append(" GROUP BY ITEM_NO");          
			
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setInt(1, this._i_store_code);
            pstmt.setString(2, item_no);
            
            rs = pstmt.executeQuery();
            
            if(rs.next()){
            	tmp.append(rs.getString("AVG_SALE_QTY"));
            } else {
            	tmp.append("");
            }
            
            rs.close();
            pstmt.close();
            
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

    private String GetWeekDays(String weekDays){

    	String Day = "";
    	
        for (int i = 0; i < 7; i++)
        {        	
        	if (weekDays.substring(i, i+1).equalsIgnoreCase("Y"))
            {   
            	switch (i)
                {
                    case 0:
                        Day += "월,";
                        break;
                    case 1:
                        Day += "화,";
                        break;
                    case 2:
                        Day += "수,";
                        break;
                    case 3:
                        Day += "목,";
                        break;
                    case 4:
                        Day += "금,";
                        break;
                    case 5:
                        Day += "토,";
                        break;
                    default:
                        Day += "일,";
                        break;
                }
            }
        }

        int index = Day.lastIndexOf(","); 
        Day = Day.substring(0, index); 
                
        return Day.trim();
    }
}
