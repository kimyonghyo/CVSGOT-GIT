/*****************************************************
 * PROGRAM ID    : ClientThread
 * PROGRAM NAME	 : 
 * CREATED BY	 : 
 * CREATION DATE : 2013.07
 *****************************************************
 *****************************************************
 *  변경일자    /  변경자  / 변경사유 
 ******************************************************/

package tesco.got;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import tesco.got.dbTran.AdsQuery;
import tesco.got.dbTran.AdsSave;
import tesco.got.dbTran.DgvSave;
import tesco.got.dbTran.Etc;
import tesco.got.dbTran.FsdQuery;
import tesco.got.dbTran.FssQuery;
import tesco.got.dbTran.FssSave;
import tesco.got.dbTran.GotException;
import tesco.got.dbTran.IacQuery;
import tesco.got.dbTran.IacSave;
import tesco.got.dbTran.ImsQuery;
import tesco.got.dbTran.ImsSave;
import tesco.got.dbTran.IodQuery;
import tesco.got.dbTran.IodSave;
import tesco.got.dbTran.LgbQuery;
import tesco.got.dbTran.LgbSave;
import tesco.got.dbTran.LgbVer;
import tesco.got.dbTran.LoginQuery;
import tesco.got.dbTran.OsiQuery;
import tesco.got.dbTran.OsiSave;
import tesco.got.dbTran.PopQuery;
import tesco.got.dbTran.PopSave;
import tesco.got.dbTran.RtcQuery;
import tesco.got.dbTran.RtiQuery;
import tesco.got.dbTran.RtiSave;
import tesco.got.dbTran.SidQuery;
import tesco.got.dbTran.TriQuery;
import tesco.got.dbTran.TriSave;
import awoo.util.Logger;
import awoo.util.Session;
import awoo.util.StringUtil;

/**
 *  연결된 클라이언트와 통신 및 명령 처리를 담당하는 클래스
 */
public class ClientThread extends Thread { 

    private BufferedInputStream bis = null;
    private BufferedOutputStream bos = null;

    private String user_id = "";
    private String store_code = "";
    private Socket socket = null;
    private String ip = "";
    private Logger logger = null;
    private Configuration config = null;
    private String IoLog = "";

    public ClientThread(Socket socket, Logger logger, Configuration config) throws SocketException,IOException {
        this.socket = socket;
        this.logger = logger;
        this.config = config;
        
        this.ip = this.socket.getInetAddress().getHostAddress();

        this.socket.setSoTimeout(10000); // 10초 ( 서버 응답 대기시간 )
       
        this.bis = new BufferedInputStream(this.socket.getInputStream());
        this.bos = new BufferedOutputStream(this.socket.getOutputStream());
    }

    /**
     * 유효한 세션인지 검사합니다.
     * 
     * @param session_key
     *                  세션ID
     * @return 유효한 세션이면 true 를 가져옵니다
     */
    private boolean checkSession(String session_key) {
        SessionManager sm = SessionManager.getInstatnce();
        Session session = sm.getSession(session_key);              
        int expireTimeOut = 180; // 60분설정 -> 2015-03-16 수정. 이종욱. 기존 60분에서 3시간으로 세션타임 증가.
        
        if (session == null) {
            return false;
        } else {                   	
        	session.setExpire_time(expireTimeOut);
        	
            if (session.isTimeout()) {
                sm.removeSession(session_key);
                return false;
            }
        }
        
        session.setLastAction();
        
        this.user_id = session.getUser_id();
        this.store_code = session.getTag().toString();
    	
        return true;
    }

    /**
     * 로그인 처리
     */
    private void executeLogin(Object[] params) throws GotException,Exception {
    	String work_flag = params[1].toString();
    	
    	if(work_flag.equals("Q")){
			if (params.length < 8) { 
				throw new GotException("PDA Version 정보가 일치하지 않습니다.\n업데이트 해주세요.");
			}
    	        
	        String user     = params[3].toString();
	        String password = params[4].toString();
	        String store    = params[5].toString();
	        String version  = params[6].toString();
	        String os_ver   = params[7].toString();
	        
	        LoginQuery lq = new LoginQuery(store, user, password, this.ip, version, os_ver);
	        String info = lq.executeQuery();	       
	   		
	        info += this.config.getUpdateServer();
	        	        	                       	       	       
	        Session session = SessionManager.getInstatnce().createSession(user);
	        session.setTag(store);

	        sendMessage(Common.LOGIN, work_flag, "0", session.getSession_key(),info);
	        
	        this.user_id = session.getUser_id();
	        this.store_code = session.getTag().toString();    	        
    	}
    }
    
    /**
     * 로그아웃 처리
     */
    private void executeLogOut(Object[] params) throws GotException,Exception {
    	String work_flag = params[1].toString();
    	
    	if(work_flag.equals("Q")){
			if (params.length < 3) { 
				throw new GotException("PDA Version 정보가 일치하지 않습니다.\n업데이트 해주세요.");
			}
	        
			String session_key = params[2].toString();
			SessionManager sm = SessionManager.getInstatnce();
			sm.removeSession(session_key);
			
			if(sm.getSession(session_key) != null)
			{
				throw new GotException(session_key + " 세션이 살아있습니다.");
			}

	        sendMessage(Common.LOGOUT, work_flag, "0", "","");
    	}
    }

    /**
     * 상품 마스터 조회
     * @param params
     */
    private void executeIms(Object[] params) throws GotException,Exception {    	
    	String work_flag = params[1].toString();
    	if (work_flag.equals("Q")) {	//상품조회  
    		
	    	if (params.length < 4) {
		        sendRequireParamMessage(Common.IMS, params[1].toString());
		        return;
	    	}
	    	
	    	String item_code = Etc.getItemCodebyBarcode(this.store_code, params[3].toString().trim());
	    	ImsQuery iq = new ImsQuery(this.store_code, item_code, this.logger);
	    	String tmp = iq.executeQuery();    	
	    	
	    	sendMessage(Common.IMS, work_flag, "0", "", tmp);
    	
    	} else if (work_flag.equals("S")) {	//진열수 전송
    		
    		if (params.length < 5) {
		        sendRequireParamMessage(Common.IMS, params[1].toString());
		        return;
	    	}
	    	
	    	String item_code = Etc.getItemCodebyBarcode(this.store_code, params[3].toString().trim());
	    	String pres_stock = params[4].toString().trim();
	    	
	    	ImsSave is = new ImsSave(this.store_code, item_code, pres_stock, this.user_id);
	    	int rejQty = is.executeQuery();    	
	    		    	
	    	sendMessage(Common.IMS, work_flag, "0", "", String.valueOf(rejQty));
    	}
    }
        
    /**
     * 일일판매수량조회(상품 마스터)
     * @param params
     */
    private void executeDis(Object[] params) throws GotException,Exception {        
    	String work_flag = params[1].toString();
           
        if (params.length < 4) {
            sendRequireParamMessage(Common.SID, params[1].toString());
            return;
        }
        
        String item_code = Etc.getItemCodebyBarcode(this.store_code, params[3].toString().trim());
        SidQuery iq = new SidQuery(this.store_code, item_code);
        String tmp = iq.executeQuery();
        
        sendMessage(Common.SID, work_flag, "0", "", tmp);
    }
    
    /**
     * 결품등록
     * @param params
     */
    private void executeOsi(Object[] params) throws GotException,Exception {        
    	String work_flag = params[1].toString();
        if (work_flag.equals("Q")) {            
        	if (params.length < 5) {
                sendRequireParamMessage(Common.OSI, params[1].toString());
                return;
            }       
        	
            String item_code = Etc.getItemCodebyBarcode(this.store_code, params[3].toString().trim());
            String group_no  = params[4].toString().trim();    
            
            OsiQuery iq = new OsiQuery(this.store_code, item_code, group_no);    
            
            String tmp = iq.executeQuery();

            sendMessage(Common.OSI, work_flag, "0", "", tmp);
            
        } else if (work_flag.equals("S")) {            
        	if (params.length < 4) {
                sendRequireParamMessage(Common.OSI, params[1].toString());
                return;
            }        
        	
            OsiSave os = new OsiSave(this.store_code, params);
            int rejQty = os.executeQuery();
            
            sendMessage(Common.OSI, work_flag, "0", "", String.valueOf(rejQty));
        }
    }
    
    /**
     * Price Card - 조회 및 등록 처리
     * @param params
     */
    private void executePop(Object[] params) throws GotException,Exception {
        String work_flag = params[1].toString();
        
        if (work_flag.equals("Q")) {
            if (params.length < 4) {
                sendRequireParamMessage(Common.POP, params[1].toString());
                return;
            }
            
            String item_code = Etc.getItemCodebyBarcode(this.store_code, params[3].toString().trim());
            PopQuery pq = new PopQuery(this.store_code,item_code);
            String tmp = pq.executeQuery();
            
            sendMessage(Common.POP, work_flag, "0", "", tmp);            
        } else if (work_flag.equals("S")) {
        	if (params.length < 4) {
                sendRequireParamMessage(Common.POP, params[1].toString());
                return;
            }
            
        	PopSave ps = new PopSave(this.store_code, params);
            ps.executeQuery();
            
            sendMessage(Common.POP, work_flag, "0", "", "");  
        }
    }      
    
    /**
     * 반품등록
     * @param params
     */
    private void executeRti(Object[] params) throws GotException,Exception {        
    	String work_flag = params[1].toString();
        if (work_flag.equals("Q")) {            
        	if (params.length < 4) {
                sendRequireParamMessage(Common.RTI, params[1].toString());
                return;
            }    
        	
            String item_code =  Etc.getItemCodebyBarcode(this.store_code, params[3].toString().trim());            
            RtiQuery rq = new RtiQuery(this.store_code, item_code);
            String tmp = rq.executeQuery();

            sendMessage(Common.RTI, work_flag, "0", "", tmp);
            
        } else if (work_flag.equals("S")) {            
        	if (params.length < 4) {
                sendRequireParamMessage(Common.RTI, params[1].toString());
                return;
            }    
        	
            RtiSave ss = new RtiSave(this.store_code, params, this.user_id);
            int rejQty = ss.executeQuery();
            
            sendMessage(Common.RTI, work_flag, "0", "", String.valueOf(rejQty));
        }
    }
           
    /**
	 * 폐기등록 
	 * @param params
	 * @throws GotException
	 * @throws Exception
	 */
	private void executeTri(Object[] params) throws GotException,Exception {
		String work_flag = params[1].toString();
        if (work_flag.equalsIgnoreCase("Q")) {
            if (params.length < 4) {
                sendRequireParamMessage(Common.TRI, params[1].toString());
                return;
            }        
            
            String item_code  = Etc.getItemCodebyBarcode(this.store_code, params[3].toString().trim()); 
            TriQuery TriQuery = new TriQuery(this.store_code, item_code);
            String responseMessage = TriQuery.executeQuery();
            
            sendMessage(Common.TRI, work_flag, "0", "", responseMessage);          
        
        } else if (work_flag.equals("S")) {
	        if (params.length < 4) {
	            sendRequireParamMessage(Common.TRI, params[1].toString());
	            return;
	        }	        
	        
	        TriSave ts = new TriSave(this.store_code, params, this.user_id);
	        String responseMessage = ts.executeQuery();
	
	        sendMessage(Common.TRI, work_flag, "0", "", responseMessage);
    	}
	}
	
	/**
     * 재고조정
     * @param params
     */
    private void executeAds(Object[] params) throws GotException,Exception {        
    	String work_flag = params[1].toString();
        if (work_flag.equals("Q")) {            
        	if (params.length < 4) {
                sendRequireParamMessage(Common.ADS, params[1].toString());
                return;
            }          
        	
            String item_code = Etc.getItemCodebyBarcode(this.store_code, params[3].toString().trim());            
            AdsQuery aq = new AdsQuery(this.store_code,item_code);
            String tmp = aq.executeQuery();
            
            sendMessage(Common.ADS, work_flag, "0", "", tmp);            
        } else if (work_flag.equals("S")) {            
        	if (params.length < 4) {
                sendRequireParamMessage(Common.ADS, params[1].toString());
                return;
            }      
        	
            AdsSave as = new AdsSave(this.store_code, params, this.user_id);
            int rejQty = as.executeQuery();            
            
            sendMessage(Common.ADS, work_flag, "0", "", String.valueOf(rejQty));
        }
    } 
	
    /**
     * 검수확인
     * @param params
     */
    private void executeIac(Object[] params) throws GotException,Exception {        
    	String work_flag = params[1].toString();
        if (work_flag.equals("Q")) {            
        	if (params.length < 7) {
                sendRequireParamMessage(Common.IAC, params[1].toString());
                return;
            }    
        	
            String item_code  = Etc.getItemCodebyBarcode(this.store_code, params[3].toString().trim());
            String recpt_date =	params[4].toString();
            String tgNo		  =	params[5].toString();
            String searchCode =	params[6].toString();
            
            IacQuery iq = new IacQuery(this.store_code, item_code, recpt_date, tgNo, searchCode);
            String tmp = iq.executeQuery();
            
            sendMessage(Common.IAC, work_flag, "0", "", tmp);            
        } else if (work_flag.equals("S")) {            
        	if (params.length < 3) {
                sendRequireParamMessage(Common.IAC, params[1].toString());
                return;
            }
        	       	
        	IacSave is = new IacSave(this.store_code, params);            
        	int rejQty = is.executeQuery();
        	
            sendMessage(Common.IAC, work_flag, "0", "", String.valueOf(rejQty));
        }
    }
    
    /**
     * 통합발주
     * @param params
     */
    private void executeIod(Object[] params) throws GotException,Exception {        
    	String work_flag = params[1].toString();
        if (work_flag.equals("Q")) {            
        	if (params.length < 4) {
                sendRequireParamMessage(Common.IOD, params[1].toString());
                return;
            }    
        	
            String item_code = Etc.getItemCodebyBarcode(this.store_code, params[3].toString().trim());            
            IodQuery pq = new IodQuery(this.store_code,item_code);
            
            // 테스트
            //String tmp = pq.executeQuery();
            String tmp = pq.executeQuery(this.logger);
            IoLog = tmp.substring(0, 1);
            
            sendMessage(Common.IOD, work_flag, "0", "", tmp);
           
        } else if (work_flag.equals("S")) {            
        	if (params.length < 6) {
                sendRequireParamMessage(Common.IOD, params[1].toString());
                return;
            }
        	
        	String item_code    = params[3].toString();
        	String qty          = params[4].toString();
        	String time_flag    = params[5].toString();
        	String order_method = params[6].toString();
        	
        	IoLog = work_flag;
	    	IodSave ms = new IodSave(this.store_code, item_code, qty, time_flag, order_method, this.user_id);
	    	
	    	// 테스트
            //ms.executeQuery();
	    	ms.executeQuery(this.logger, this.ip);
            
            sendMessage(Common.IOD, work_flag, "0", "", "");
        }
    }     
    
    /**
     * RTC 라벨 출력 조회
     * @param params
     */
    private void executeRtc(Object[] params) throws GotException,Exception {
    	
    	String work_flag = params[1].toString();
    	if (params.length < 4) {
	        sendRequireParamMessage(Common.RTC, params[1].toString());
	        return;
    	}             
    	
    	String item_code = Etc.getItemCodebyBarcode(this.store_code, params[3].toString().trim());
    	RtcQuery rq = new RtcQuery(this.store_code, item_code);
    	String tmp = rq.executeQuery();    	
    	
    	sendMessage(Common.RTC, work_flag, "0", "", tmp);    	
    }   
    
    /**
     * 신선재고조사 날짜 조회
     * @param params
     */
    private void executeFsd(Object[] params) throws GotException,Exception {
        
    	String work_flag = params[1].toString();        

        if (work_flag.equals("Q")) {
            
            FsdQuery fs = new FsdQuery(this.store_code);
            String tmp = fs.executeQuery();

            sendMessage(Common.FSD, work_flag, "0", "", tmp);            
        }
    }
    
    /**
     * 신선재고조사
     * @param params
     */
    private void executeFss(Object[] params) throws GotException,Exception {        
    	String work_flag = params[1].toString();
        if (work_flag.equals("Q")) {            
        	if (params.length < 4) {
                sendRequireParamMessage(Common.FSS, params[1].toString());
                return;
            }      
        	
            String item_code   = Etc.getItemCodebyBarcode(this.store_code, params[3].toString().trim());
            String survey_date = params[4].toString().trim();
            
            FssQuery fq = new FssQuery(this.store_code,item_code, survey_date);
            String tmp = fq.executeQuery();
            
            sendMessage(Common.FSS, work_flag, "0", "", tmp);            
        } else if (work_flag.equals("S")) {            
        	if (params.length < 5) {
                sendRequireParamMessage(Common.FSS, params[1].toString());
                return;
            }            
        	
        	String ip          = params[3].toString();
        	String survey_date = params[4].toString();
        	
            FssSave fs = new FssSave(this.store_code, ip, survey_date, params, this.user_id);
            fs.executeQuery();     
            
            sendMessage(Common.FSS, work_flag, "0", "", "");
        }
    }
    
    /**
     * 디지털상품권 재고조사 저장
     * @param params
     */
    private void executeDgv(Object[] params) throws GotException,Exception {
        
    	String work_flag = params[1].toString();        

        if (work_flag.equals("S")) {
           
            String dgv_type	=	params[3].toString();
        	
            ArrayList dgvNoList = new ArrayList();
            for (int i=4; i<params.length; i++) {
                dgvNoList.add(params[i].toString());
            }
            
            DgvSave dgvSave = new DgvSave(this.store_code, dgv_type, dgvNoList);            
            dgvSave.executeQuery();
            
            sendMessage(Common.DGV, work_flag, "0", "", "");           
        }
    }    
//    
//    /**
//     * 통합발주 수동발주
//     * @param params
//     */
//    private void executeImo(Object[] params) throws GotException,Exception {        
//    	String work_flag = params[1].toString();
//        if (work_flag.equals("Q")) {            
//        	if (params.length < 4) {
//                sendRequireParamMessage(Common.IMO, params[1].toString());
//                return;
//            }     
//        	
//            String item_code =  params[3].toString().trim();            
//            ImoQuery mq = new ImoQuery(this.store_code,item_code);
//            String tmp = mq.executeQuery();
//
//            sendMessage(Common.IMO, work_flag, "0", "", tmp);           
//        }  else if (work_flag.equals("S")) {            
//    		if (params.length < 6) {
//	            sendRequireParamMessage(Common.IMO, params[1].toString());
//	            return;
//	        }
//    		
//	    	String item_code = params[3].toString();
//	    	String qty       = params[4].toString();
//	    	String time_flag = params[5].toString();
//	    	
//	    	IomSave ms = new IomSave(this.store_code, item_code, qty, time_flag, this.user_id);
//	        ms.executeQuery();
//            
//            sendMessage(Common.IMO, work_flag, "0", "", "");
//        }
//    }
//    
//    /**
//     * 통합발주 신선발주
//     * @param params
//     */
//    private void executeIfo(Object[] params) throws GotException,Exception {    	
//    	String work_flag = params[1].toString();        
//    	if (work_flag.equalsIgnoreCase("Q")) {
//            if (params.length < 4) {
//                sendRequireParamMessage(Common.IFO, params[1].toString());
//                return;
//            }           
//            
//            String item_code = params[3].toString().trim(); 
//            IfoQuery fq = new IfoQuery(this.store_code,item_code);
//            String tmp = fq.executeQuery();
//            
//            sendMessage(Common.IFO, work_flag, "0", "", tmp);                          
//    	}  else if (work_flag.equals("S")) {            
//    		if (params.length < 6) {
//	            sendRequireParamMessage(Common.IFO, params[1].toString());
//	            return;
//	        }
//    		
//	    	String item_code = params[3].toString();
//	    	String qty       = params[4].toString();
//	    	String time_flag = params[5].toString();
//	    	
//	    	IomSave ms = new IomSave(this.store_code, item_code, qty, time_flag, this.user_id);
//	        ms.executeQuery();
//
//	        sendMessage(Common.IFO, work_flag, "0", "", "");
//      	}    	
//    }
//    
//    /**
//     * 통합발주 행사발주
//     * @param params
//     */
//    private void executeIpo(Object[] params) throws GotException,Exception {        
//    	String work_flag = params[1].toString();
//        if (work_flag.equals("Q")) {            
//        	if (params.length < 4) {
//                sendRequireParamMessage(Common.IPO, params[1].toString());
//                return;
//            }       
//        	
//            String item_code =  params[3].toString().trim();            
//            IpoQuery pq = new IpoQuery(this.store_code,item_code);
//            String tmp = pq.executeQuery();
//
//            sendMessage(Common.IPO, work_flag, "0", "", tmp);           
//        } else if (work_flag.equals("S")) {            
//        	if (params.length < 6) {
//                sendRequireParamMessage(Common.IPO, params[1].toString());
//                return;
//            }
//        	
//        	String item_code = params[3].toString();
//        	String qty       = params[4].toString();
//        	String time_flag = params[5].toString();
//	    	
//	    	IomSave ms = new IomSave(this.store_code, item_code, qty, time_flag, this.user_id);
//            ms.executeQuery();
//            
//            sendMessage(Common.IPO, work_flag, "0", "", "");
//        }
//    }        
//    
//    /**
//     * 로그북
//     * @param params
//     */
//    private void executeLgb(Object[] params) throws GotException,Exception {        
//    	String work_flag = params[1].toString();
//        if (work_flag.equals("V")) {            
//        	if (params.length < 3) {
//                sendRequireParamMessage(Common.LGB, params[1].toString());
//                return;
//            }     
//                        
//            LgbVer lg  = new LgbVer(this.store_code);
//            String tmp = lg.executeQuery();
//            
//            sendMessage(Common.LGB, work_flag, "0", "", tmp);                    
//        }else if (work_flag.equals("Q")) {            
//        	if (params.length < 4) {
//                sendRequireParamMessage(Common.LGB, params[1].toString());
//                return;
//            }     
//        	
//        	String log_id = params[3].toString();
//            LgbQuery lq   = new LgbQuery(log_id);
//            String tmp    = lq.executeQuery();
//            
//            sendMessage(Common.LGB, work_flag, "0", "", tmp);
//        }else if (work_flag.equals("S")) {            
//        	if (params.length < 4) {
//                sendRequireParamMessage(Common.LGB, params[1].toString());
//                return;
//            }
//        	
//        	String date	=	params[3].toString();            
//        	LgbSave ls  = new LgbSave(this.store_code, date, params, this.user_id);
//            ls.executeQuery();
//            
//            sendMessage(Common.LGB, work_flag, "0", "", "");
//        }
//    }
    
    /**
     * 클라이언트 요청 처리
     * @param params
     */
    private void parseCommand(Object[] params) {
        String command 	   = "";
        String work_flag   = "";
        String session_key = "";
        
        try{
            command 		= params[0].toString();
            work_flag 		= params[1].toString();
            session_key 	= params[2].toString();
            
            if (!command.equals(Common.LOGIN)) {
                if (session_key.equals("")) {
                    sendNotLoginMessage();
                    return;
                }
                if (!checkSession(session_key)){
                    sendSesstionTimeOutMessage(); 
                    return; 
                }
            }     
                        
            if (command.equals(Common.LOGIN)) {
                executeLogin(params);
            } 
            //else if (command.equals(Common.LOGOUT)) { // 로그아웃 - 테스트 중
            //    executeLogOut(params);
            //} 
            else if (command.equals(Common.IMS)) { // 상품조회
                executeIms(params);
            } else if (command.equals(Common.OSI)) { // 결품등록
            	executeOsi(params);
            } else if (command.equals(Common.SID)) {
                executeDis(params);
            } else if (command.equals(Common.POP)) {
                executePop(params);          
			} else if (command.equals(Common.RTC)) {
				executeRtc(params);
			} else if (command.equals(Common.TRI)) {
				executeTri(params);						
            } else if (command.equals(Common.ADS)) {
            	executeAds(params);
            } else if (command.equals(Common.RTI)) {
            	executeRti(params);
            } else if (command.equals(Common.FSD)) {
            	executeFsd(params);
            } else if (command.equals(Common.FSS)) {
            	executeFss(params);
            } else if (command.equals(Common.DGV)) {
            	executeDgv(params);
            } else if (command.equals(Common.IAC)) {
            	executeIac(params);
            } else if (command.equals(Common.IOD)) {
            	executeIod(params);
            	work_flag = IoLog;	// 통합발주의 유형에 따라 구분하기 위해
//            } else if (command.equals(Common.LGB)) {
//            	executeLgb(params);            	
            } else {
                sendInvalidCommandMessage();
                return;
            }
            
            //전문헤더 내용만 로그로 남김
            if(this.logger != null){
                if (command.equals(Common.IAC) || command.equals(Common.IOD)) {
                	
                	String logParams = "";                	
                	for(int i = 0; i < params.length; i++)
                		logParams += params[i].toString() + " : ";
                	
                	// 2015-07-02 수정. 이종욱. 자바 하위 버전에서 params 객체 이름, 주소 등이 찍히는 것을 수정. 
                	//this.logger.writeEntry("[" + this.ip + "] : " + params);		
                	this.logger.writeEntry("[" + this.ip + "] : " + logParams);
                	
                } else {                
                	this.logger.writeEntry("[" + this.ip + "]\t" + this.user_id + "\t " + command + "\t" + work_flag + "\t" + this.store_code);
                }
            } 
            
        } catch (GotException e){
           	sendErrorMessage(command, work_flag, e.getMessage());
        } catch (Exception e){
            sendErrorMessage(command, work_flag, e);            
        }        
    }
       
    public void run() {
        byte[] buffer = new byte[4096];
        try {
            String packet = "";

            while (this.bis.read(buffer) != 0) {
                packet += new String(buffer);
                if (!packet.startsWith(Common.STX)) {
                    sendInvalidPacketMessage(Common.ERROR, "");
                    return;
                }
                int end = -1;
                if ((end = packet.indexOf(Common.ETX)) >= 0) {
                    packet = packet.substring(1, end);
                    break;
                }
            }
                        
            Object[] params = StringUtil.split(packet, Common.FS);
           
            /** 로그추적 */
            String command = params[0].toString();
                        
            parseCommand(params);            
          
        } catch (InterruptedIOException e1) {
            try{
                sendCommandTimeOutMessage();
            } catch(Exception e){}
            
        } catch (Exception e2) {            
            try{
                sendErrorMessage(Common.ERROR, "", e2);                
            } catch(Exception e3){}
            
        } finally {
            try {
                this.bis.close();
            } catch (Exception e) {
            }
            try {
                this.bos.flush();
                this.bos.close();
            } catch (Exception e) {
            }
            try {
                this.socket.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * 클라이언트로 메세지 전달
     * @param command 클라이언트로 부터 전달받은 명령어
     * @param work_flag 클라이언트로 부터 잔달받은 작업구분
     * @param res_code 응답 코드 (")" - 성공 / "1" - 실패)
     * @param res_msg 에러 메세지
     * @param param 파라미터
     */
    private void sendMessage(String command, String work_flag, String res_code,
            String res_msg, String param) {
        try {
            StringBuffer sb = new StringBuffer(Common.STX);
            sb.append(command);
            sb.append(Common.FS);
            sb.append(work_flag);
            sb.append(Common.FS);
            sb.append(res_code);
            sb.append(Common.FS);
            sb.append(res_msg);
            sb.append(Common.FS);
            sb.append(param);
            sb.append(Common.ETX);
            sb.append("\0");
            
            //this.bos.write(sb.toString().getBytes("KSC5601"));
            this.bos.write(sb.toString().getBytes("EUC-KR"));
            this.bos.flush();

        } catch (Exception e) {
            if(this.logger != null){
                this.logger.writeEntry(e);
            }
        }
    }
    
    private void sendErrorMessage(String command, String work_flag, String err_msg) {
        sendMessage(command, work_flag, "1", err_msg, "");
// 		L4(?)에서 서비스 체크를 위해서 소캣을 지속적으로 날리기 때문에 로그는 남기지 않도록 한다.
//        if(this.logger != null){
//        	this.logger.writeEntry("[" + this.ip + "]\t" + this.user_id + "\t" + command + " \t" + work_flag + "\tError : " + err_msg);
//        }
    }

    /**
     * @param command
     * @param work_flag
     * @param err_msg
     */
    private void sendErrorMessage(String command, String work_flag, Exception e) {
        sendMessage(command, work_flag, "1", e.getMessage(), "");        
        if(this.logger != null){
        	this.logger.writeEntry("[" + this.ip + "]\t" + this.user_id + "\t" + command + "\t" + work_flag + "\tError : " + e.getMessage());
        }
    }
    
    /**
     * 세션이 만료되었음
     */
    private void sendSesstionTimeOutMessage() {
        sendErrorMessage(Common.SESSION_TIME_OUT, "", "로그인 시간이 만료되었습니다.\r\n재로그인 해주세요");
    }
    
    /**
     * 로그인 이 되지 않았음
     */
    private void sendNotLoginMessage() {
        sendErrorMessage(Common.NOT_LOGIN, "", "로그인 하지 않았습니다.\r\n로그인 해주세요");
    }

    /**
     * 파라미터 갯수가 부족합
     * @param command
     * @param work_flag
     */
    private void sendRequireParamMessage(String command, String work_flag) {
        sendErrorMessage(command, work_flag, "파라미터 갯수가 부족합니다.");
    }

    /**
     * 소켓 연결 후 명령이 전달되지 않을 경우 에러 전달
     * @param command "ERR"
     * @param work_flag
     */
    private void sendCommandTimeOutMessage() {
        sendErrorMessage(Common.COMMAND_TIMEOUT, "", "명령이 완전히 전달되지 않았습니다.");
    }

    /**
     * 올바르지 않은 명령어 에러
     * @param command "ERR"
     * @param work_flag
     */
    private void sendInvalidCommandMessage() {
        sendErrorMessage(Common.INVALID_COMMAND, "", "사용할 수 없는 명령입니다.");
    }

    /**
     * STX 로 시작하지 않거나 ETX 로 끝나지 않는 패킷
     * @param command
     * @param work_flag
     */
    private void sendInvalidPacketMessage(String command, String work_flag) {
        sendErrorMessage(command, work_flag, "패킷 오류입니다.");
    }   
}