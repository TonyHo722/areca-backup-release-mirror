package com.application.areca.external;

import java.io.File;
import java.io.IOException;

import com.application.areca.AbstractArecaLauncher;
import com.application.areca.ApplicationException;
import com.application.areca.impl.policy.EncryptionPolicy;
import com.myJava.commandline.BooleanCmdLineOption;
import com.myJava.commandline.CmdLineParserException;
import com.myJava.commandline.CommandLineParser;
import com.myJava.commandline.StringCmdLineOption;
import com.myJava.file.FileSystemIterator;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.file.driver.DefaultFileSystemDriver;
import com.myJava.file.driver.DriverAlreadySetException;
import com.myJava.file.driver.FileSystemDriver;

/**
 * <BR>
 * @author Ludovic QUESNELLE
 * <BR>
 * <BR>Areca Build ID : 1926729655347670856
 */
 
 /*
 Copyright 2005-2007, Olivier PETRUCCI.
 
This file is part of Areca.

    Areca is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    Areca is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Areca; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
public class CmdLineDeCipher 
extends AbstractArecaLauncher {
    
    private static final String ARG_ALG = "algorithm";
    private static final String ARG_PASSWD = "password";
    private static final String ARG_SOURCE = "source";
    private static final String ARG_FILE = "file";
    private static final String ARG_DESTINATION = "destination";
    private static final String ARG_SHOW = "l";
    
    private static final String DESCRIPTION = 
        "Areca's external decryption tool.\ndecrypt -" 
        + ARG_SOURCE + "=[" + ARG_SOURCE + "] -"
        + ARG_ALG + "=[" + ARG_ALG + "] -"
        + ARG_PASSWD + "=[" + ARG_PASSWD + "] -"
        + ARG_DESTINATION + "=[" + ARG_DESTINATION + "]"
        ;
    
	private String algorithm="";
	private String encryption="";
	private String mountPoint="";
	private String source="";
	private String targetDir="";
	private boolean justShow=false;
	
    public static void main(String[] args) {
        CmdLineDeCipher launcher = new CmdLineDeCipher();
        launcher.launch(args);
    }
    
	public CmdLineDeCipher() {
	}
		
	public boolean init(String args[]) {
		CommandLineParser parser=new CommandLineParser();
        parser.setDescription(DESCRIPTION);
        
		parser.addParameter(new StringCmdLineOption(true,ARG_ALG,"Encryption algorithm [DESede_HASH, AES_HASH]"));
		parser.addParameter(new StringCmdLineOption(true,ARG_PASSWD,"Key phrase to use for decoding"));
		parser.addParameter(new StringCmdLineOption(true,ARG_SOURCE,"Source directory"));
		parser.addParameter(new StringCmdLineOption(false,ARG_FILE,"Specific file to extract"));
		parser.addParameter(new StringCmdLineOption(true,ARG_DESTINATION,"Destination Directory "));
		parser.addParameter(new BooleanCmdLineOption(false,ARG_SHOW,"Display only mode"));
		
		try {
			parser.parse(args, null);
			
			algorithm =  (String)parser.getParameter(ARG_ALG).getValue();
			encryption=  (String)parser.getParameter(ARG_PASSWD).getValue();
			mountPoint=  (String)parser.getParameter(ARG_SOURCE).getValue();
			source    =  (String)parser.getParameter(ARG_FILE).getValue();
			targetDir =  (String)parser.getParameter(ARG_DESTINATION).getValue();
			justShow  =  ((Boolean)parser.getParameter(ARG_SHOW).getValue()).booleanValue();
			
		} catch (CmdLineParserException e) {
			System.out.println("Syntax error : " + e.getMessage());
			System.out.println(parser.usage());
			return false;
		}	
		return true;
	}
	
	protected void initializeFileSystemManager() {
		EncryptionPolicy policy=new EncryptionPolicy();
		policy.setEncrypted(true);
		policy.setEncryptionAlgorithm(algorithm);
		policy.setEncryptionKey(encryption);
		
		try {
		    File mnt = new File(mountPoint);
			FileSystemDriver driver=policy.initFileSystemDriver(mnt, new DefaultFileSystemDriver());
			FileSystemManager.getInstance().registerDriver(mnt, driver);
		} catch (ApplicationException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		} catch (DriverAlreadySetException e) {
			System.out.println(e);
		}
	}
		
	protected void process() throws IOException{
		if(justShow) {
			display();
		} else {
			FileTool fileMgr=FileTool.getInstance();
			String fileToProcess=(source!=null) ? source  : mountPoint;
			fileMgr.copy(new File(fileToProcess), new File(targetDir));
		}
	}
	
	protected void display() {
		String fileToProcess=(source!=null) ?source  : mountPoint;
		FileSystemIterator iterator=new FileSystemIterator(new File(fileToProcess), false);
        showLine();
		while(iterator.hasNext()){
			File currentFile=(File)iterator.next();
			System.out.println(currentFile.getAbsoluteFile());
		}
        showLine();
	}

    protected void launchImpl(String[] args) {
		try {
            // Here Parse the commandLine
            // -Alg=Algo -Sen=Sentence -Source=source -Dest=destination
            CmdLineDeCipher deCipher = new CmdLineDeCipher();

            if (deCipher.init(args)) {
            	deCipher.initializeFileSystemManager();
            	deCipher.process();
            }
        } catch (Throwable e) {
            System.out.println("\nWARNING : An error occured during decryption. You should check that all your arguments are valid (encryption algorithm or password, source directory, ...)");
            showLine();
            e.printStackTrace();
            showLine();
        }
	}
}
