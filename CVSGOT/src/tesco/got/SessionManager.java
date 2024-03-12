package tesco.got;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import awoo.util.Session;

public class SessionManager
{
    private static SessionManager thisObj = new SessionManager();
    private Hashtable ht;

    private SessionManager()
    {
        ht = new Hashtable();
    }

    public static SessionManager getInstatnce()
    {
        if(thisObj == null)
            thisObj = new SessionManager();
               		
        return thisObj;
    }
    
    public synchronized void killTimeoutSession(int timeOut)
    {
    	Collection c = ht.values();
        Iterator itr = c.iterator();
        Session sessionObj = null;
        
        while (itr.hasNext()){        	
        	sessionObj = (Session)itr.next();        
        	sessionObj.setExpire_time(timeOut);
        	
        	if(sessionObj.isTimeout())
        		ht.remove(sessionObj.getSession_key());
        }
    }

    public synchronized Session createSession(String user_id)
    {
        Session session = new Session(user_id);
        String session_key = session.getSession_key();
        
        ht.put(session_key, session);
        return session;
    }

    public synchronized Session removeSession(String session_key)
    {
        return (Session)ht.remove(session_key);
    }

    public synchronized Session getSession(String session_key)
    {
        return (Session)ht.get(session_key);
    }
}