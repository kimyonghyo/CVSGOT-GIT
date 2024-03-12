/*****************************************************
 * PROGRAM ID    : LoginQuery
 * PROGRAM NAME	 : 사용자 로그인
 * CREATION DATE : 2013
 *****************************************************
 *****************************************************
 *  변경일자    /  변경자  / 변경사유

 ******************************************************/

package tesco.got.dbTran;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import tesco.got.Common;
import awoo.dbUtil.ConnPool;
import awoo.dbUtil.OraConnFactory;

public class LoginQuery {
	private String _s_store_code = "";
	private int    _i_store_code = 0;
    private String _user         = "";
    private String _password     = "";
    private String _ip           = "";
    private String _version      = "";
    private String _os_version   = "";
    
    /**
     * @param user	사용자 아이디
     * @param password	비밀번호
     */
    public LoginQuery(String store_code, String user, String password, String ip, String version, String os_version){
    	this._s_store_code = store_code;
    	this._i_store_code = Integer.parseInt(store_code);
    	this._user 		   = user;
        this._password 	   = password;
        this._ip           = ip; 
        this._version      = version;
        this._os_version   = os_version;
    }
    
    /**
     * @throws Exception
     */
    public String executeQuery() throws GotException,Exception{          	
    	
    	Connection conn = null;
        ConnPool cp = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        StringBuffer sql = null;
    	    	
        String user_auth = "";
        String store_name = "";                       
        String name_k = "";
        String server_time = "";
        String scs_user = "cvsapp";
        String scs_pass = "cvs123";
        
        String pdaVersion = "";
		String newVersion = "";
		String alertMsg = "";		
		String patchUseYn = "";
		String Dir = "";
		String CheckVer = "F"; 
		String Use_YN = "N"; 
        
        try{
            cp = ConnPool.getInstance();
            conn = cp.getConnection();
            
            sql = new StringBuffer();
            sql.append("SELECT CASE WHEN TO_CHAR(SYSDATE, 'yyyyMMddhh24') >= OPEN_DATE THEN 'Y' ELSE 'N' END USE_YN ");
            sql.append("  FROM PDA_OPEN_SCHEDULE ");
            sql.append(" WHERE STORE_NO = ?      ");
            
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setInt(1,this._i_store_code);
            rs = pstmt.executeQuery();
            
            if(rs.next()){
            	String useYn = rs.getString("USE_YN");            	
            	if(useYn.equals("N")) {
            		throw new GotException("신규 시스템이 아직 오픈되지 않았습니다");
            	}
            } 
            
            rs.close();
            pstmt.close();
            
            sql = new StringBuffer();
            sql.append("SELECT STORE_NAME 	 ");
            sql.append("  FROM STORES        ");
            sql.append(" WHERE STORE_NO  = ? ");
            
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setInt(1,this._i_store_code);
            rs = pstmt.executeQuery();
            
            if(rs.next()){
            	store_name = rs.getString("STORE_NAME");            	
            } else {
            	throw new GotException("점포코드를 확인해주세요. ");
            }
            
            rs.close();
            pstmt.close();
                       
            sql = new StringBuffer();      
            sql.append("SELECT NAME_K, STORE_NO,							 		 ");            
            sql.append("	   CASE WHEN STORE_NO = '999' OR STORE_NO = ? THEN 'Y'   ");
            sql.append("	   		ELSE 'N'									     ");
            sql.append("	    END AUTH_CD,										 ");
            sql.append("       TO_CHAR(SYSDATE, 'YYYYMMDDHH24MISS') AS SERVER_TIME   ");
            sql.append("  FROM EMPLOYEE   						 ");
            sql.append(" WHERE RPAD(TRIM(EMPLOYEE_NO),10,'0') = ?");

            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setInt(1,this._i_store_code);
            pstmt.setString(2, this._user);
            
            rs = pstmt.executeQuery();
                                   
            if(rs.next()){
            	user_auth   = rs.getString("AUTH_CD");
            	name_k      = rs.getString("NAME_K");
            	server_time	= rs.getString("SERVER_TIME");
            	
            	if(user_auth.equals("N")) {
            		throw new GotException("사용자 정보와 점포정보가 일치하지 않습니다");
            	}
            }else{
            	throw new GotException("User ID 오류입니다.");
            }                      
            
            rs.close();
            pstmt.close();
                        		 
            sql = new StringBuffer();
			sql.append("SELECT STORE_NO, PDA_IP, NVL(PDA_VER, '0.0.1') PDA_VER, NVL(NEW_VER, '0.0.1') NEW_VER, DIR, NVL(ALERT_MSG, ' ') AS ALERT_MSG,  "); 
			sql.append("  	   NVL(USE_YN, 'N') USE_YN ");
			sql.append("  FROM PDA_VERSION  ");
			sql.append(" WHERE STORE_NO = ? ");
			sql.append("   AND PDA_IP   = ? ");
             
            pstmt = conn.prepareStatement(sql.toString());
         	pstmt.setString(1, this._s_store_code);
         	pstmt.setString(2, this._ip);
         	
 			rs = pstmt.executeQuery();

 			if(rs.next()){  
 				pdaVersion = rs.getString("PDA_VER");                        
 				newVersion = rs.getString("NEW_VER");
 				alertMsg   = rs.getString("ALERT_MSG");
                patchUseYn = rs.getString("USE_YN");
                Dir 	   = rs.getString("DIR");
            } else {
            	pdaVersion = "1.0.0";
            	newVersion = "1.0.0";
                alertMsg   = "";
                patchUseYn = "N";                     
                Dir        = "/CVSNAS1/PDA/release";
            }

            rs.close();
            pstmt.close();
                         
            String Dbver[] =  pdaVersion.split("\\.");                        
          	String Upver[] = newVersion.split("\\.");
            String pdaver[] = this._version.split("\\.");
          	
          	String db_version = ""; // db에 저장된 version                      	
          	String up_version = ""; // update 될 version
          	String cl_version = ""; // client version
          	
          	for(int i =0 ; i< 3; i++ ){
          		db_version+= Dbver[i];                      		
          		up_version+=Upver[i];
          		cl_version+= pdaver[i];                      		
          	}                 	
          	
          	if(Integer.parseInt(db_version) > Integer.parseInt(cl_version) || Integer.parseInt(up_version) > Integer.parseInt(cl_version) )
          	{                 		
          		CheckVer = "T"; // 자동 다운로드
          		Use_YN = "Y";
          	}
           	
           	if(patchUseYn.equalsIgnoreCase("Q")){
       			CheckVer = "Q"; // 강제 다운로드 실행
       		}
           	
            VersionSave ver = new VersionSave(this._s_store_code, this._version, this._ip, Dir, this._user, Use_YN, this._os_version);
            ver.executeQuery();
          	
			StringBuffer tmp = new StringBuffer(name_k);
			tmp.append(Common.FS);
			tmp.append(this._s_store_code);
			tmp.append(Common.FS);
			tmp.append(store_name);
			tmp.append(Common.FS);
			tmp.append(scs_user);		// scs user
			tmp.append(Common.FS);
			tmp.append(scs_pass);		// scs pw
			tmp.append(Common.FS);
			// PDA Version Check
			tmp.append(Dir);			// update server directory
			tmp.append(Common.FS);			
			tmp.append(newVersion);		// db version
			tmp.append(Common.FS);
			tmp.append(alertMsg);		// update/alert message
			tmp.append(Common.FS);
			tmp.append(patchUseYn);		// use_yn
			tmp.append(Common.FS);
			tmp.append(CheckVer);		// type (강제/자동)					
			tmp.append(Common.FS);
			// Server Time
			tmp.append(server_time);
			tmp.append(Common.FS);

           	 // 재고폐기사유
            sql = new StringBuffer();      
            sql.append(" SELECT CODE_ID_INT, CODE_DESC FROM CODES 	 ");
            sql.append("  WHERE code_group=10010		   			 ");
            sql.append("    AND CODE_ID_INT > 50 order by CODE_ID_INT");
	   		
	   		pstmt = conn.prepareStatement(sql.toString());	   		
	   		rs = pstmt.executeQuery();
	   		
	   		while (rs.next()) {
	   			tmp.append(rs.getString("CODE_ID_INT"));
	   			tmp.append("-");
	   			tmp.append(rs.getString("CODE_DESC"));
	   			tmp.append(Common.GS);
            }	   		
	   		
	   		rs.close();
	   		pstmt.close();
	   		
	   		tmp.append(Common.FS);
	   		
	   		//반품사유
            sql = new StringBuffer();
	   		sql.append("SELECT CODE_ID_INT, CODE_DESC FROM CODES   ");
	   		sql.append(" WHERE CODE_GROUP = 10011 ORDER BY CODE_ID_INT   ");
         	
         	pstmt = conn.prepareStatement(sql.toString());	   		
	   		rs = pstmt.executeQuery();
	   		
         	while (rs.next()) {
	   			tmp.append(rs.getString("CODE_ID_INT"));
	   			tmp.append("-");
	   			tmp.append(rs.getString("CODE_DESC"));
	   			tmp.append(Common.GS);
            }
         	
	   		rs.close();
	   		pstmt.close();
         		   		
	   		tmp.append(Common.FS);
	   		
	   		//등록시 제약사항 (수량/금액)
            sql = new StringBuffer();
	   		sql.append("SELECT CODE_DESC FROM CODES   ");
	   		sql.append(" WHERE CODE_GROUP = 10300 ORDER BY CODE_ID_INT");
         	
         	pstmt = conn.prepareStatement(sql.toString());	   		
	   		rs = pstmt.executeQuery();
	   		
         	while (rs.next()) {
	   			tmp.append(rs.getString("CODE_DESC"));
	   			tmp.append(Common.GS);
            }
         	
	   		rs.close();
	   		pstmt.close();	   		
	   			   		
	   		tmp.append(Common.FS);
	   		
	   		/* RTC 용지 소진시 제거   Start -- 추후 용지 소진시 제거 */
	   		String useRtcStores = "701^704^755^702^705^706^708^709^710^711^712^713^714^717^718^719^720^721^722^723^724^725^726^727^728^729^730^731^732^773^734^735^736^737^738^739^740^741^742^744^745^746^747^748^749^750^751^752^753^754^756^757^758^759^760^761^762^763^764^765^766^767^768^769^770^771^772^776^778^6016^";
	   		String rtcUseYn = useRtcStores.indexOf(this._s_store_code) >= 0 ? "Y" : "N";
	   		
	   		tmp.append(rtcUseYn);
	   		tmp.append(Common.FS);
	   		/* RTC 용지 소진시 제거   End */	   		
	   			   		
            updateUserConnectIPandTime(conn);
            
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
            } 
            catch(Exception e){}
        }
    }
    
    public void updateUserConnectIPandTime(Connection conn) throws GotException,Exception {
        PreparedStatement pstmt = null;
        try{
        	conn.setAutoCommit(false);
        	
            String strSql = " UPDATE EMPLOYEE      		   ";
            strSql +=  		"   SET CONNECT_IP = ? 		   "; 
            strSql +=  		"     , CONNECT_DATE = SYSDATE ";
            strSql +=  		" WHERE EMPLOYEE_NO = ? 	   ";
           
            pstmt = conn.prepareStatement(strSql);
            pstmt.setString(1,this._ip);
            pstmt.setString(2,this._user);
            
            pstmt.executeUpdate();
            pstmt.close();    
            
            conn.commit();
    	} catch(SQLException se) {
    		try{
                if (pstmt != null)
                    pstmt.close();
            } catch(Exception e){}    		
        	try {
        		conn.rollback();
        	} catch (Exception e) {}        	
        	throw se;
    	}
    }   
}
