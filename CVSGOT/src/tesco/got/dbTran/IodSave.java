/*****************************************************
 * PROGRAM ID    : IodSave
 * PROGRAM NAME  : ���չ��� ����κ�
 * CREATION DATE : 2013
 * 2014-06-27 / �����/ ���ְ��ɽð� ����
 * 2015-03-18 / ������/ ���ֻ��� �ð� ����(���� 07:00 -> 05:00)�� ���� �ð� ����
 *****************************************************/ 
package tesco.got.dbTran;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import awoo.dbUtil.ConnPool;
import awoo.dbUtil.OraConnFactory;
import awoo.util.Logger;

public class IodSave {

	private String _s_store_code = "";
	private int    _i_store_code = 0;
	private String _barcode      = "";	
	private String _qty 		 = "";
	private String _time_flag 	 = "";
	private String _order_method = "";
	private String _user_id 	 = "";
	
    public IodSave(String store_code, String barcode, String qty, String time_flag, String order_method, String user_id) {
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
	public void executeQuery(Logger logger, String ip) throws GotException,Exception {
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
	    String m_hns_store = "0";	/* Hub Spoke ���� �߰� - HUB SPOKE ���� ���� '1'*/        
        /*��� ���ַ� ���� ���� �߰�*/
	    String section			="";
        int ordAvgQty =0; /*��� ��� ���ַ�*/
        int ordQty = 0;/*��� ���� + ���� ���ַ�*/
        int ordRecQty = 0;/*����� ���Է�+ �԰����� */

        // 2014-12-03 ������ ������ 2014�⿡ ���ؼ��� ���� - �߰� ����
	      String yyyy			="";        
        // 2014-12-03 ������ ������ 2014�⿡ ���ؼ��� ���� - �߰� ���� 
        
		try {
			
			/*���ּ����� 1000���� ���� �� ����*/
			if(Integer.parseInt(this._qty)  >= 1000 )
			{
				throw new GotException("���� ������ 1000 ���� ���� �� �����ϴ�.");
			
			}
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
            
            /*��� �������� üũ*/
            
            sql = new StringBuffer();
            // 2014-12-03 ������ ������ 2014�⿡ ���ؼ��� ���� - ���� ����           
            //sql.append("SELECT A.SALES_CLASS_NO AS SECTION_CD ");
            sql.append("SELECT A.SALES_CLASS_NO AS SECTION_CD, TO_CHAR(SYSDATE,'YYYY') AS YYYY ");  
            // 2014-12-03 ������ ������ 2014�⿡ ���ؼ��� ���� - ���� ����   
            sql.append("  FROM PLU A ");
            sql.append("   WHERE A.ITEM_NO  = ?");
            sql.append("   AND A.STORE_NO = ?");     
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setString(1, this._barcode);
            pstmt.setInt(2, this._i_store_code); 
            rs = pstmt.executeQuery();
            
            if(rs.next()){
            	section = rs.getString("SECTION_CD");
            	// 2014-12-03 ������ ������ 2014�⿡ ���ؼ��� ���� - �߰� ����        
            	yyyy = rs.getString("YYYY");            	
            	// 2014-12-03 ������ ������ 2014�⿡ ���ؼ��� ���� - �߰� ����                    	
            }
            
            
            rs.close();
            pstmt.close();
            
            //Trial ���� �߰� ���� ��ü �����  �����ڵ� üũ �κ� ���� �ʿ� 20140919 
            // 2014-12-03 ������ ������ 2014�⿡ ���ؼ��� ���� - ���� ����        
            //if(section.equalsIgnoreCase("330111012"))
            if(section.equalsIgnoreCase("330111012") && yyyy.equalsIgnoreCase("2014") )            
            // 2014-12-03 ������ ������ 2014�⿡ ���ؼ��� ���� - ���� ����                    
            {
            	sql = new StringBuffer();
	            sql.append("SELECT FNC_GET_AVG_QTY(?) AS AVGQTY, FNC_GET_REC_QTY(?) AS RECQTY,FNC_GET_ORD_QTY(?) AS ORDQTY ");
	            sql.append("  FROM DUAL ");
	            
	            pstmt = conn.prepareStatement(sql.toString());
	            pstmt.setInt(1, this._i_store_code); 
	            pstmt.setInt(2, this._i_store_code); 
	            pstmt.setInt(3, this._i_store_code); 
	            rs = pstmt.executeQuery();
	            
	            if(rs.next()){
	            	
	            	ordAvgQty	= rs.getInt("AVGQTY");	
	            	ordRecQty	= rs.getInt("RECQTY");
	            	ordQty	= rs.getInt("ORDQTY");
	            
	            
	            }
	            else{
	            	throw new GotException("���� ������ ��ǰ�� �ƴմϴ�(���).\n����� ���� ��Ź �帳�ϴ�.");
	            }
	            
	            if(ordAvgQty - (ordRecQty +ordQty) < Integer.parseInt(this._qty))
	            {
	            	throw new GotException("�� ��� ���� ���� :"+Integer.toString(ordAvgQty)+" ��\n��� ���� ����(�԰�������):  "+Integer.toString(ordRecQty)+" ��\n���� ���� ����(���� ���� ����):"+Integer.toString(ordQty)+" ��\n�߰� ���� ���� ����: "+Integer.toString(ordAvgQty - (ordRecQty +ordQty))+" �� �Դϴ�");
	            }
	            // 2014-12-03 ��� ���� �߰� ���� - ���� ���� 100��
              if(Integer.parseInt(this._qty)  > 100 )
			        {
				        throw new GotException("��� ��ǰ ���� ���� ������ 100�� ���� �Դϴ�.");
         			}	            
	            // 2014-12-03 ��� ���� �߰� ���� - ���� ���� 100��         			
	            
	            rs.close();
	            pstmt.close();
	            //throw new GotException("���� ������ ��ǰ�� �ƴմϴ�(���)."+section);
            }
                              
            /*
             * 2015-03-18 ����. ������. ���ֻ��� �ð� ����(���� 07:00 -> 05:00)�� ���� �ð� ����
             */
            sql = new StringBuffer();
//            sql.append("SELECT CASE WHEN TO_CHAR(SYSDATE,'HH24MI') BETWEEN '0700' AND '0800' THEN 'F'"		);
            sql.append("SELECT CASE WHEN TO_CHAR(SYSDATE,'HH24MI') BETWEEN '0500' AND '0530' THEN 'F'"		);
            sql.append("       		ELSE 'T'"																);
            sql.append("       	END ORDER_FALSE, "															);    
            sql.append("   	   CASE WHEN TO_CHAR(SYSDATE,'HH24MI') BETWEEN '0000' AND '0500' OR TO_CHAR(SYSDATE,'HH24:MI') > '" + last_order_time + "' THEN 'DD'");            
            sql.append("       		ELSE 'D'"																);
            sql.append("       	END DAY_CHECK,"																);
            sql.append("       CASE WHEN TO_CHAR(SYSDATE,'HH24MI') BETWEEN '0000' AND '0500' THEN TO_CHAR(SYSDATE,'yyyymmdd')"	);
            sql.append("       		ELSE TO_CHAR(SYSDATE+1,'yyyymmdd')"														  	);
            sql.append("       	END ORDER_DATE,"																				);            
            sql.append("       CASE WHEN TO_CHAR(TRUNC(SYSDATE, 'MI'), 'HH24:MI') > SSMTIME THEN 'O'"							);
            sql.append("            WHEN TO_CHAR(SYSDATE,'HH24MI') BETWEEN '0000' AND '0500' THEN 'O'"							);
            sql.append("       		ELSE 'U'"																					);
            sql.append("       	END ORDER_TIME_FLAG,"																			);
            sql.append("       B.ITEM_NO,"																						);
/* Hub Spoke ���� �߰� - Start */            
            sql.append("       NVL(TO_CHAR((SELECT COUNT(*) FROM MASTER_MNC WHERE STORE_NO = B.STORE_NO AND ROWNUM = 1)),'0') HNS_STORE ");            
/* Hub Spoke ���� �߰� - End */            
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
/* Hub Spoke ���� �߰� - Start */
              m_hns_store	 = rs.getString("HNS_STORE");            	              	            
/* Hub Spoke ���� �߰� - End */              
            }else
            {            	
            	throw new GotException("���� �ð� ������ ���� ���� Section�Դϴ�.\n����� ���� ��Ź �帳�ϴ�.");
            }
            
            rs.close();
            pstmt.close();
            
            if(orderFalse.equalsIgnoreCase("F"))
            {
//            	throw new GotException("���� �Ұ����� �ð� �Դϴ�.\nAM 07:00~08:00 ���� ����Ʈ ���� �ð��Դϴ�.");
               	throw new GotException("���� �Ұ����� �ð� �Դϴ�.\nAM 05:00~05:30 ���� ����Ʈ ���� �ð��Դϴ�.");
             }
            
            // ���ָ����ð�(SSMTIME - 10:30) ~ ���ָ����ð� + 30�� ���̿��� ���ָ������� ��������
            if(order_time_flag.equalsIgnoreCase("O")){
            	if(day_check.equalsIgnoreCase("D")){
            		throw new GotException("���� ���� �Ǿ����ϴ�.\n���� ���ɽð��� "+ last_order_time +" �Դϴ�.");
            	}            	
            }
            
            /*
            // 2015-04-01 �߰�. ������. Ư����ǰ ���ְ��� ����
            if(item_code.equalsIgnoreCase("124705026") && Integer.parseInt(this._qty) > 12)
            {
            	throw new GotException("���ּ������� ���Ѽ����� 12 ���� �ʰ��� ������ �Է��Ͽ����ϴ�.");
            }
            */
            
            // 2015-02-17 �߰�. ������. ����ǰ ����Ÿ�� ��� ����.
            /*
            Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
            String curTime = sdf.format(now);
            
            sql = new StringBuffer();
            sql.append("SELECT ITEM_NO, COUNT(ITEM_NO) AS CNT");
            sql.append("  FROM DAIRY_PRODUCT_TMP ");
            sql.append("  WHERE ITEM_NO = ? ");
            sql.append("  GROUP BY  ITEM_NO ");
            
            pstmt = conn.prepareStatement(sql.toString());     
            pstmt.setString(1, item_code);
            rs = pstmt.executeQuery();			
			
            if( Integer.parseInt(curTime) > 201502231300 && Integer.parseInt(curTime) < 201502241030 )
            {
            	if(rs.next() && rs.getInt("CNT") != 0)
            	{
            		throw new GotException("����ǰ ����Ÿ�� ��ҷ� ���� ���ְ� �Ұ����մϴ�.");
            	}
            }       
            */
            // 2015-02-17 �߰�.
            
            rs.close();
            pstmt.close();
            
            if(this._time_flag.equalsIgnoreCase("O")){
            	//���ֽð��� ���� �Ǿ��ٸ�
    			sql = new StringBuffer();
    			sql.append("SELECT COUNT(ITEM_NO)  ");
    			
				/* Hub Spoke ���� �߰� - Start */            
		          if(m_hns_store.equalsIgnoreCase("1")){                
		              sql.append("  FROM ORDER_MANUAL_FU_MNC ");
		          }else{
		              sql.append("  FROM ORDER_MANUAL_FU ");            	
		          }
				/* Hub Spoke ���� �߰� - End */                            			
    			sql.append(" WHERE STORE_NO     = ?");
    			sql.append("   AND ITEM_NO      = ?");
    			sql.append("   AND ORDER_DATE   = TO_CHAR(SYSDATE+1,'YYYYMMDD')");
    			// 2014-12-03 �ڵ����� ��ǰ������ Ÿ�� POG��ǰ�� ��� ���� ���� - ���� ����
    			//sql.append("   AND ORDER_METHOD = 'A'"							);
    			sql.append("   AND ORDER_METHOD = 'A' AND FNC_ITEM_ORDERABLE(STORE_NO,ITEM_NO) <> 'Y' "							);    			
    			// 2014-12-03 �ڵ����� ��ǰ������ Ÿ�� POG��ǰ�� ��� ���� ���� - ���� ����    			
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
    				throw new GotException("�ڵ� ���� ��ǰ �Դϴ�.\n���� ��� �� �� �����ϴ�. : " + this._barcode);
    			}
         		
         		pstmt.close();
         		rs.close();

            	sql = new StringBuffer();
            	
            	// 2015-12-14 �߰�. ������. ���� DB ���� �ð� �׽�Ʈ�� �ӽ�.
            	logger.writeEntry("[" + ip + "]\t" + "[���չ��� �ֹ� �� UPDATE ORDER_MANUAL_FU]\t���� : " + this._i_store_code + "\t������ : " + item_code + "\t���� : " + this._qty);
            	
            	/* Hub Spoke ���� �߰� - Start */            
            	
          		if(m_hns_store.equalsIgnoreCase("1")){                
          		    sql.append("UPDATE ORDER_MANUAL_FU_MNC ");
          		}else{
          		    sql.append("UPDATE ORDER_MANUAL_FU ");            	
          		}
      			// Hub Spoke ���� �߰� - End                             			            	
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
         			throw new GotException("���� �����  �Ҽ� �����ϴ� : " + this._barcode);
            	}
         		
         		logger.writeEntry("[" + ip + "]\t" + "[���չ��� UPDATE ORDER_MANUAL_FU]\t���� : " + this._i_store_code + "\t������ : " + item_code + "\t���� : " + this._qty);
         		
         		pstmt.close();         		         		   
             
            }
            else
            {
    			//���ֽð� �̳� ��� �߰�����    
	            	
    			sql = new StringBuffer();
    			sql.append("SELECT COUNT(ITEM_NO)    ");
    			
    			/* Hub Spoke ���� �߰� - Start */    
  	            if(m_hns_store.equalsIgnoreCase("1")){                
  	            	sql.append("  FROM ORDER_MANUAL_MNC ");
	            }else{
	            	sql.append("  FROM ORDER_MANUAL ");            	
	            }
          		// Hub Spoke ���� �߰� - End                             			
  	           
    			sql.append(" WHERE STORE_NO     = ?  ");
    			sql.append("   AND ITEM_NO      = ?  ");
    			sql.append("   AND ORDER_DATE   = TO_CHAR(SYSDATE,'YYYYMMDD')   ");
    			// 2014-12-03 �ڵ����� ��ǰ������ Ÿ�� POG��ǰ�� ��� ���� ���� - ���� ����
    			//sql.append("   AND ORDER_METHOD = 'A'"							);
    			sql.append("   AND ORDER_METHOD = 'A' AND FNC_ITEM_ORDERABLE(STORE_NO,ITEM_NO) <> 'Y' "							);    			
    			// 2014-12-03 �ڵ����� ��ǰ������ Ÿ�� POG��ǰ�� ��� ���� ���� - ���� ����    			    			
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
    				throw new GotException("�ڵ� ���� ��ǰ �Դϴ�.\n���� ��� �� �� �����ϴ�. : " + this._barcode);
    			}
    			
    			rs.close();
         		pstmt.close();         		
            		
    			sql = new StringBuffer(); 
    			
    			// 2015-12-14 �߰�. ������. ���� DB ���� �ð� �׽�Ʈ�� �ӽ�.
            	logger.writeEntry("[" + ip + "]\t" + "[���չ��� �ֹ� �� UPDATE ORDER_MANUAL]\t���� : " + this._i_store_code + "\t������ : " + item_code + "\t���� : " + this._qty);
    			
    			/* Hub Spoke ���� �߰� - Start */            
    			
		          if(m_hns_store.equalsIgnoreCase("1")){                
		              sql.append("UPDATE ORDER_MANUAL_MNC ");
		          }else{
		              sql.append("UPDATE ORDER_MANUAL ");            	
		          }
          		// Hub Spoke ���� �߰� - End                             			            	    			
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
            		throw new GotException("���� �����  �Ҽ� �����ϴ� : " + this._barcode);
            	}
         		
         		logger.writeEntry("[" + ip + "]\t" + "[���չ��� UPDATE ORDER_MANUAL]\t���� : " + this._i_store_code + "\t������ : " + item_code + "\t���� : " + this._qty);
         		
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
	
	

