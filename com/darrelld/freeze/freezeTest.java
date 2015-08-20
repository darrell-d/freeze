package com.darrelld.freeze;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;

import org.junit.Test;

import com.amazonaws.auth.AWSCredentials;

public class freezeTest {

	@Test
	public void testUserAuth() throws FileNotFoundException {
		UserAuth authTest = new UserAuth();
		assertNotNull(authTest);
		assertTrue(authTest.getCredentials() instanceof AWSCredentials);
		
	}

}
