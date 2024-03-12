/*****************************************************
 * PROGRAM ID    : LgbVer
 * PROGRAM NAME  : 로그북 Version 및 파일 조회
 * CREATED BY    : 이태성
 * CREATION DATE : 2012/02/07
 *****************************************************/ 
package tesco.got.dbTran;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.io.BufferedReader;
import java.io.FileReader;

import tesco.got.Common;
import awoo.dbUtil.ConnPool;
import awoo.dbUtil.OraConnFactory;


public class LgbVer {
	
	private String store_code = ""; 
    
	public LgbVer(String store_code) {
    	this.store_code = store_code;
    }
    
    public String executeQuery() throws GotException,Exception{
    	StringBuffer sql = null;
    	StringBuffer sql1 = null;
        
        Connection conn = null;
        ConnPool cp = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try{
            cp = ConnPool.getInstance();
            conn = cp.getConnection();
            
            String date = "";
            String version = "";
            String ip	= "";         
            String user = "";
            String pass = "";
            String dir = "";

            sql = new StringBuffer();
            sql.append("   SELECT TO_CHAR(MAX(PARM_DATE),'YYYYMMDD') AS PARM_DATE  ");
            sql.append("   FROM PARAMETER   ");
            sql.append("   WHERE PARM_NAME = 'BUSINESS_DATE'   ");
            
			pstmt = conn.prepareStatement(sql.toString());
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				date	=	rs.getString("PARM_DATE");
			}
			
			rs.close();
            pstmt.close();
            
            sql1 = new StringBuffer();
            sql1.append("   SELECT SCS_USER, SCS_PASS, DIR, LOGBOOK_VER, LOGBOOK_SERVER   ");
            sql1.append("   FROM PDA_VERSION   ");
            sql1.append("   WHERE STORE_NO = ?  ");
            
            pstmt = conn.prepareStatement(sql1.toString());
			
            pstmt.setString(1, this.store_code);
            
            rs = pstmt.executeQuery();
			
			if(rs.next()){
				user = rs.getString("SCS_USER");
				pass = rs.getString("SCS_PASS");
				dir = rs.getString("DIR");				
				version = rs.getString("LOGBOOK_VER");
				ip = rs.getString("LOGBOOK_SERVER");
			}
 /*           
			//String iniFileName = "/product/scs/pda/BIN/Tesco.ini";
            String iniFileName = "PDA.INI";
			FileReader fr = new FileReader(iniFileName);
			BufferedReader br = new BufferedReader(fr);
			
			String temp = "";
			String key = "";
			String val = "";			
			
			while((temp = br.readLine()) != null)
			{
				//주석 무시
				if(temp.trim().charAt(0)=='#')
					continue;
				if(temp.trim().charAt(0)=='[')
					continue;
				//key = val 
				key = temp.substring(0,temp.indexOf("=")-1).trim().toUpperCase();
				val = temp.substring(temp.indexOf("=")+1).trim();
				if ( key.equals( "BOOKVERSIO")) {
					version = val;
				}else if ( key.equals( "BOOKI")) {
					ip = val;
				}else if ( key.equals( "BOOKPOR")) {
					port = val;
				}else if ( key.equals( "BOOKUSE")) {
					user = val;
				}else if ( key.equals( "BOOKPAS")) {
					pass = val;
				}else if ( key.equals( "BOOKHOME_DI")) {
					dir = val;
				}
			}
			
			if(version == null || version ==""){
				throw new GotException("로그북 정보 읽기 실패!");
			}
			*/
			StringBuffer tmp = new StringBuffer();
            tmp.append(version);
            tmp.append(Common.FS);
            tmp.append(date);
            tmp.append(Common.FS);
            tmp.append(ip);
            tmp.append(Common.FS);
            tmp.append(user);
            tmp.append(Common.FS);
            tmp.append(pass);
            tmp.append(Common.FS);
            tmp.append(dir);
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