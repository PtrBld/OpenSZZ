package com.SZZ.app;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.SZZ.jiraAnalyser.Application;
import com.SZZ.jiraAnalyser.entities.Transaction;
import com.SZZ.jiraAnalyser.entities.TransactionManager;
import com.SZZ.jiraAnalyser.git.Git;
import com.SZZ.jiraAnalyser.git.JiraRetriever;

public class SZZApplication {
	
	  /* Get actual class name to be printed on */
	static Logger log = Logger.getLogger(SZZApplication.class.getName());
	public static final String DEFAULT_BUG_TRACKER = "/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml";
	
	public static void main(String[] args)  {
		if (args.length == 0) {
			System.out.println("Welcome to SZZ Calculation script.");
			System.out.println("Here a guide how to use the script");
			System.out.println("szz.jar -d jiraUrl jiraKey");
			System.out.println("The script saves the file faults.csv containing the issues reported in Jira");
			System.out.println("szz.jar -l gitRepositoryPath");
			System.out.println(
					"This script saves the file gitlog.csv containing the parsed gitlog with all the information needed to execute szz");
			System.out.println("szz.jar -m gitRepositoryPath");
			System.out.println(
					"the script takes in input the files generated before (faults.csv and gitlog.csv) and generate the final result in the file FaultInducingCommits.csv");
			System.out.println("szz.jar -all githubUrl, jiraKey => all steps together");
		} else {
			switch (args[0]) {
			case "-d":
				if (args.length == 1){
					System.out.println("Jira key not given");
					break;
				}
		
				JiraRetriever jr = new JiraRetriever(args[1] + DEFAULT_BUG_TRACKER, log, args[2]);
				if (jr.testURL()){
				jr.printIssues();
				jr.combineToOneFile();
				}
				else{
					System.out.println("JiraKey is wrong or is not of the apache foundation");
					break;
				}
		
				break;
			case "-l":
				Git g = new Git((new File(args[1]).toPath()));
				try {
					g.saveLog();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case "-m":
				TransactionManager tm = new TransactionManager();
				List<Transaction> transactions =  tm.getBugFixingCommits(args[1]);
				Application a = new Application(args[1]);
				Git g1 = new Git((new File(args[2]).toPath()));
				a.calculateBugFixingCommits(transactions);
				a.calculateBugInducingCommits(g1);
				break;
			case "-all":
				Git git;
				try{	
				JiraRetriever jr1 = new JiraRetriever(args[2]+DEFAULT_BUG_TRACKER, log, args[3]);
				jr1.printIssues();
				jr1.combineToOneFile();
				}
				catch(Exception e){
					break;
				}
				try {
					git = new  Git((new File(args[3])).toPath(), new URL(args[1]));
					git.cloneRepository();
					git.saveLog();
					TransactionManager t1 = new TransactionManager();
					List<Transaction> transactions1 =  t1.getBugFixingCommits(args[3]);
					Application a1 = new Application(args[3]);
					a1.calculateBugFixingCommits(transactions1);
					//a1.calculateBugInducingCommits(git);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				break;
			default:
				System.out.println("Commands are not in the right form! Please retry!");
				break;

			}
		}

	}
}
