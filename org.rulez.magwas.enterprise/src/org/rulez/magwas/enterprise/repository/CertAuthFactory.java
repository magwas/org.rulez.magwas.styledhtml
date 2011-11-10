// shamelessly stolen from http://postgresql.1045698.n5.nabble.com/attachment/4405851/0/CertAuthFactory.java
/*-------------------------------------------------------------------------
*
* Copyright (c) 2011, PostgreSQL Global Development Group
*
*
*-------------------------------------------------------------------------
*/
package org.rulez.magwas.enterprise.repository;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;



/**
 * Provide an implementation of SSLSocketFactory that allows client authentication
 * @author Marc-André Laverdière (marc-andre@atc.tcs.com / marcandre.laverdiere@tcs.com)
 */
public class CertAuthFactory extends SSLSocketFactory {

	public final static String CONFIG_KEYSTORE_PATH = "org.postgresql.jdbc.keystore.path";
	public final static String CONFIG_KEYSTORE_PWD = "org.postgresql.jdbc.keystore.password";
	public final static String CONFIG_TRUSTSTORE_PATH = "org.postgresql.jdbc.truststore.path";
	public final static String CONFIG_TRUSTSTORE_PWD = "org.postgresql.jdbc.truststore.password";
	
	public final static String SSL_PROTOCOL_NAME = "SSL";
	public final static String SECURE_RANDOM_NAME = "SHA1PRNG";
	
	protected final SSLSocketFactory _internalFactory;
	
	public CertAuthFactory() throws IOException, GeneralSecurityException {
		SSLContext context = buildSSLContext();
		_internalFactory = context.getSocketFactory();
	}
	
	public CertAuthFactory(String ignored) throws IOException, GeneralSecurityException {
		SSLContext context = buildSSLContext();
		_internalFactory = context.getSocketFactory();
	}
	
	/**
	 * Builds an SSLContext with the specified trust store and key store
	 * */
	protected static SSLContext buildSSLContext() throws IOException, GeneralSecurityException{
		FileInputStream fInKeyStore = null;
		FileInputStream fInTrustStore = null;
		try{
			//Load configuration
			String trustPath = System.getProperty(CONFIG_TRUSTSTORE_PATH);
			String trustPwd = System.getProperty(CONFIG_TRUSTSTORE_PWD);
			String keyPath = System.getProperty(CONFIG_KEYSTORE_PATH);
			String keyPwd = System.getProperty(CONFIG_KEYSTORE_PWD);
			System.out.println("keyPath="+keyPath);
			System.out.println("trustPath="+trustPath);
			KeyManagerFactory managerFactory = null;
			TrustManagerFactory trustFactory = null;
			if (keyPath != null && !"".equals(keyPath)){
				//Load the Key Managers
				KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
				fInKeyStore = new FileInputStream(keyPath);
				ks.load(fInKeyStore, keyPwd.toCharArray());
			    managerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			    managerFactory.init(ks, keyPwd.toCharArray());
			    System.out.println("managerFactory="+managerFactory);
			}
			if (trustPath != null && !"".equals(trustPath)){
			    // Load the trust store
				KeyStore trustKs = KeyStore.getInstance(KeyStore.getDefaultType());
				fInTrustStore = new FileInputStream(keyPath);
				trustKs.load(fInTrustStore, trustPwd.toCharArray());
			    trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			    trustFactory.init(trustKs);
			}
		    //Create + Initialize TLS context
		    SSLContext context = SSLContext.getInstance(SSL_PROTOCOL_NAME); //can be TLS too
		    KeyManager[] kms = managerFactory.getKeyManagers();
		    TrustManager[] tms = trustFactory.getTrustManagers();
		    SecureRandom sr = SecureRandom.getInstance(SECURE_RANDOM_NAME);
		    System.out.println("context:"+kms+tms+sr);
			context.init(kms, tms, sr);
			return context;
		} finally{
			try{
				if (fInKeyStore != null)
					fInKeyStore.close();
				if (fInTrustStore != null)
					fInTrustStore.close();
			} catch (IOException e){
				//ignore it
			} 
		} 
	}
	
	/**
	 * Enables the client mode and the client authentication
	 * */
	protected SSLSocket enableClientAuth(SSLSocket sock){
		sock.setNeedClientAuth(true);
		sock.setUseClientMode(true);
		return sock;
	}
	
	@Override
	public Socket createSocket() throws IOException {
		SSLSocket sock = (SSLSocket) _internalFactory.createSocket();
		return enableClientAuth(sock);
	}
	
	@Override
	public Socket createSocket(InetAddress address, int port, InetAddress localhost, int localPort)
			throws IOException {
		SSLSocket sock = (SSLSocket) _internalFactory.createSocket(address, port, localhost, localPort);
		return enableClientAuth(sock);
	}
	
	@Override
	public Socket createSocket(InetAddress host, int port) throws IOException {
		SSLSocket sock = (SSLSocket) _internalFactory.createSocket(host, port);
		return enableClientAuth(sock);
	}
	
	@Override
	public Socket createSocket(Socket s, String host, int port, boolean autoClose)
			throws IOException {
		SSLSocket sock = (SSLSocket) _internalFactory.createSocket(s, host, port, autoClose);
		return enableClientAuth(sock);
	}

	@Override
	public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
		SSLSocket sock = (SSLSocket) _internalFactory.createSocket(host, port);
		return enableClientAuth(sock);
	}
	
	@Override
	public Socket createSocket(String host, int port, InetAddress localAddress, int localPort)
			throws IOException, UnknownHostException {
		SSLSocket sock = (SSLSocket) _internalFactory.createSocket(host, port, localAddress, localPort);
		return enableClientAuth(sock);
	}

	@Override
	public String[] getDefaultCipherSuites() {
		return _internalFactory.getDefaultCipherSuites();
	}

	@Override
	public String[] getSupportedCipherSuites() {
		return _internalFactory.getSupportedCipherSuites();
	}

}