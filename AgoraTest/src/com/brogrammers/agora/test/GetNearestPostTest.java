package com.brogrammers.agora.test;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.brogrammers.agora.Agora;
import com.brogrammers.agora.data.CacheDataManager;
import com.brogrammers.agora.data.ESDataManager;
import com.brogrammers.agora.helper.QuestionLoaderSaver;
import com.brogrammers.agora.model.Answer;
import com.brogrammers.agora.model.Author;
import com.brogrammers.agora.model.Comment;
import com.brogrammers.agora.model.Question;
import com.brogrammers.agora.model.SimpleLocation;
import com.brogrammers.agora.views.MainActivity;
import com.google.gson.Gson;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

public class GetNearestPostTest extends ActivityInstrumentationTestCase2<MainActivity> {

	public GetNearestPostTest() {
		super(MainActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		HttpClient client = new DefaultHttpClient();
		try {
			HttpDelete deleteRequest = new HttpDelete("http://cmput301.softwareprocess.es:8080/cmput301f14t02/GetNearestPostTest/_mapping");
			client.execute(deleteRequest);
			String mapping="{ \"GetNearestPostTest\": {\n"+
					" \"properties\": {\n"+
					" \"answers\": {\n"+
					" \"type\": \"nested\", \n"+
					" \"properties\": {\n"+
					" \"author\": {\n"+
					" \"type\": \"string\"\n"+
					" },\n"+
					" \"body\": {\n"+
					" \"type\": \"string\"\n"+
					" },\n"+
					" \"comments\": {\n"+
					" \"properties\": {\n"+
					" \"author\": {\n"+
					" \"properties\": {\n"+
					" \"username\": {\n"+
					" \"type\": \"string\"\n"+
					" }\n"+
					" }\n"+
					" },\n"+
					" \"body\": {\n"+
					" \"type\": \"string\"\n"+
					" },\n"+
					" \"date\": {\n"+
					" \"type\": \"long\"\n"+
					" },\n"+
					" \"posted\": {\n"+
					" \"type\": \"boolean\"\n"+
					" }\n"+
					" }\n"+
					" },\n"+
					" \"date\": {\n"+
					" \"type\": \"long\"\n"+
					" },\n"+
					" \"hasImage\": {\n"+
					" \"type\": \"boolean\"\n"+
					" },\n"+
					" \"rating\": {\n"+
					" \"type\": \"long\"\n"+
					" },\n"+
					" \"uniqueID\": {\n"+
					" \"type\": \"long\"\n"+
					" }\n"+
					" }\n"+
					" },\n"+
					" \"author\": {\n"+
					" \"type\": \"string\"\n"+
					" },\n"+
					" \"body\": {\n"+
					" \"type\": \"string\"\n"+
					" },\n"+
					" \"comments\": {\n"+
					" \"properties\": {\n"+
					" \"author\": {\n"+
					" \"properties\": {\n"+
					" \"username\": {\n"+
					" \"type\": \"string\"\n"+
					" }\n"+
					" }\n"+
					" },\n"+
					" \"body\": {\n"+
					" \"type\": \"string\"\n"+
					" },\n"+
					" \"date\": {\n"+
					" \"type\": \"long\"\n"+
					" },\n"+
					" \"posted\": {\n"+
					" \"type\": \"boolean\"\n"+
					" }\n"+
					" }\n"+
					" },\n"+
					" \"date\": {\n"+
					" \"type\": \"long\"\n"+
					" },\n"+
					" \"hasImage\": {\n"+
					" \"type\": \"boolean\"\n"+
					" },\n"+
					" \"location\": {\n"+
					" \"type\": \"geo_point\" \n"+
					" },\n"+
					" \"rating\": {\n"+
					" \"type\": \"long\"\n"+
					" },\n"+
					" \"title\": {\n"+
					" \"type\": \"string\"\n"+
					" },\n"+
					" \"uniqueID\": {\n"+
					" \"type\": \"long\"\n"+
					" }\n"+
					" }\n"+
					" }\n"+
					" }";
			HttpPost httppost = new HttpPost("http://cmput301.softwareprocess.es:8080/cmput301f14t02/GetNearestPostTest/_mapping");
			httppost.setEntity(new StringEntity(mapping));
			httppost.setHeader("Accept", "application/json");
			client.execute(httppost);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	private class TestESManager extends ESDataManager {
		public TestESManager() {
			super("http://cmput301.softwareprocess.es:8080/", "cmput301f14t02/", "GetNearestPostTest/");
		}
	}
	
	private class TestCacheManager extends CacheDataManager {
		public TestCacheManager() {
			super("TEST_CACHE");
		}
	}
	
	public void testESGetQuestions() throws Throwable {
		// create a question object post it, add a comment locally to one of the answers.
		Question q = new Question("Big Questions", "What do you think the meaning of life is?", null, "Ted");
		Answer a = new Answer("Not really sure", null, "Bill");
		q.addAnswer(a);
		q.addAnswer(new Answer("I mean who really knows?", null, "Bob"));
		Answer b = new Answer("This post doesn't belong here.", null, "Tim");
		q.setLocation(new SimpleLocation(53.526797, 113.52735));
		q.setLocationName("Edmonton");

		Question q1 = new Question("Big Things", "Wow", null, "Bob");
		q1.setLocation(new SimpleLocation(51.05, 114.06));
		q1.setLocationName("Calgary");

		Question q2 = new Question("grand things", "Wow", null, "Tim");
		q2.setLocation(new SimpleLocation(49.25, 123.1));
		q2.setLocationName("Vancouver");

		// update the server with the new question
		final ESDataManager es = new TestESManager();
		//final CacheDataManager cache = new TestCacheManager();
		final CountDownLatch postSignal = new CountDownLatch(1);
		es.pushQuestion(q2);
		es.pushQuestion(q);
		es.pushQuestion(q1);
		postSignal.await(2, TimeUnit.SECONDS);
		
		// get the question from the server
		final List<ArrayList<Question>> results = new ArrayList<ArrayList<Question>>();
		final CountDownLatch signal = new CountDownLatch(1);
		runTestOnUiThread(new Runnable() {
			public void run() {
					results.add((ArrayList<Question>)es.searchQuestionsByLocation(new SimpleLocation(53.526797, 113.52735)));
			}
		});
		
		// ensure is empty before receiving any response
		assertTrue("Received a result before one was expected", results.get(0).size() == 0);
		try {
			signal.await(3, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			assertTrue(false);
		}
		
		// compare the local and received copies to ensure the server
		// copy matches the local copy.
		Gson gson = new Gson();
		// ensure we have only 1 result
		assertTrue(results.get(0).size() == 1);
		String jsonReceivedQuestionNearest = gson.toJson(results.get(0).get(0));
		// String jsonReceivedQuestionSecondNearest = gson.toJson(results.get(0).get(1));
		// String jsonReceivedQuestionThirdNearest = gson.toJson(results.get(0).get(2));
		assertTrue("Nearest question was not first result", gson.toJson(q).equals(jsonReceivedQuestionNearest));
		// assertTrue("Second nearest question was not first result", gson.toJson(q1).equals(jsonReceivedQuestionSecondNearest));
		// assertTrue("Nearest question was not first result", gson.toJson(q2).equals(jsonReceivedQuestionThirdNearest));
	}
}

	